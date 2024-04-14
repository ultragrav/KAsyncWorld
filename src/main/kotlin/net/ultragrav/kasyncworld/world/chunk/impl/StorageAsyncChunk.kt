package net.ultragrav.kasyncworld.world.chunk.impl

import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.ticks.SavedTick
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.IntCounts
import net.ultragrav.kasyncworld.world.chunk.block.count.TypeCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.iteration.NormalIteration
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.palette.SimplePalette
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImpl
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImplConfig
import net.ultragrav.kasyncworld.world.chunk.heightmap.AsyncHeightMap
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.section.AsyncChunkSection
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBiome
import java.util.*

class StorageAsyncChunk(override val heightOptions: ChunkHeightOptions) : AsyncChunk {

    override val sections: Array<AsyncChunkSection?> = arrayOfNulls(heightOptions.numSections)
    override val heightMaps: MutableMap<Heightmap.Types, AsyncHeightMap> = Heightmap.Types.entries
        .associateWith { AsyncHeightMap(it, this) }
        .toMutableMap()

    override val blockEntities: MutableMap<AWBlockPosition, CompoundTag> = mutableMapOf()
    override val entities: MutableList<CompoundTag> = mutableListOf()

    override var blockTicks: MutableList<SavedTick<Block>> = mutableListOf()
    override var fluidTicks: MutableList<SavedTick<Fluid>> = mutableListOf()

    override var persistentData: CompoundTag = CompoundTag()

    override fun setBlock(x: Int, y: Int, z: Int, block: BlockState) {
        val section = getOrMakeSection(y shr 4)
        val before = section.getBlock(x, y and 15, z)
        section.setBlock(x, y and 15, z, block)
        heightMaps.values.forEach { it.update(x, y, z, block) }
        if (before.hasBlockEntity()) {
            removeBlockEntity(x, y, z)
        }
        if (block.hasBlockEntity()) {
            val tag = (block.block as EntityBlock).newBlockEntity(BlockPos(0, 0, 0), block)
                ?.saveWithId() ?: return
            setBlockEntity(x, y, z, tag)
        }
    }

    override fun isBlockSet(x: Int, y: Int, z: Int): Boolean {
        val section = getSection(y shr 4) ?: return false
        return section.isBlockSet(x, y and 15, z)
    }

    override fun unsetBlock(x: Int, y: Int, z: Int) {
        val section = getOrMakeSection(y shr 4)
        section.unsetBlock(x, y and 15, z)
        removeBlockEntity(x, y, z)
    }

    override fun getBlock(x: Int, y: Int, z: Int): BlockState {
        val section = getSection((y shr 4))
            ?: return Blocks.AIR.defaultBlockState()
        return section.getBlock(x, y and 15, z)
    }

    override fun types(): Set<BlockState> {
        return sections.filterNotNull()
            .flatMap { it.blocks.types() }
            .toSet()
    }

    override fun contains(state: BlockState): Boolean {
        return sections.filterNotNull()
            .any { state in it.blocks }
    }

    override fun getBiome(x: Int, y: Int, z: Int): org.bukkit.block.Biome {
        val section = getSection(y shr 2) ?: return org.bukkit.block.Biome.PLAINS
        val holder = section.getBiome(x, y and 3, z)
        return CraftBiome.minecraftHolderToBukkit(holder)
    }

    override fun setBiome(x: Int, y: Int, z: Int, biome: org.bukkit.block.Biome) {
        val section = getOrMakeSection(y shr 2)
        section.setBiome(x, y and 3, z, CraftBiome.bukkitToMinecraftHolder(biome))
    }

    override fun getHeightMap(type: Heightmap.Types): AsyncHeightMap {
        return heightMaps.getOrPut(type) { AsyncHeightMap(type, this) }
    }

    override fun setHeightMap(type: Heightmap.Types, heightMap: AsyncHeightMap) {
        if (heightMap.heightOptions != this.heightOptions) {
            throw IllegalArgumentException("Height map has different height options")
        }

        heightMaps[type] = heightMap.clone()
    }

    override fun clearHeightMaps() {
        heightMaps.clear()
    }

    override fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag? {
        return blockEntities[AWBlockPosition(x, y, z)]
    }

    override fun setBlockEntity(x: Int, y: Int, z: Int, tag: CompoundTag) {
        blockEntities[AWBlockPosition(x, y, z)] = tag
    }

    override fun removeBlockEntity(x: Int, y: Int, z: Int) {
        blockEntities.remove(AWBlockPosition(x, y, z))
    }

    override fun clearBlockEntities() {
        blockEntities.clear()
    }

    override fun setSection(sectionIndexMinBased: Int, section: AsyncChunkSection?) {
        sections[sectionIndexMinBased - heightOptions.minSection] = section
    }

    override fun getSection(sectionIndexMinBased: Int): AsyncChunkSection? {
        return sections[sectionIndexMinBased - heightOptions.minSection]
    }

    private fun getOrMakeSection(sectionY: Int): AsyncChunkSection {
        val shifted = sectionY - heightOptions.minSection
        return sections[shifted] ?: createSection().also { sections[shifted] = it }
    }

    override fun clearSections() {
        sections.fill(null)
    }

    override fun addEntity(tag: CompoundTag) {
        entities.add(tag)
    }

    override fun removeEntity(tag: CompoundTag) {
        entities.remove(tag)
    }

    override fun clearEntities() {
        entities.clear()
    }

    override fun clone(): AsyncChunk {
        val copy = StorageAsyncChunk(heightOptions)
        copy.entities.addAll(entities)
        copy.blockTicks.addAll(blockTicks)
        copy.fluidTicks.addAll(fluidTicks)
        copy.persistentData = persistentData.copy()
        copy.blockEntities.putAll(blockEntities)
        copy.heightMaps.putAll(heightMaps)
        sections.forEachIndexed { index, section ->
            copy.sections[index] = section?.clone()
        }
        return copy
    }

    override fun hash(): Int {
        val blockTickHash = blockTicks.map { SavedTick.UNIQUE_TICK_HASH.hashCode(it) }
            .hashCode()
        val fluidTickHash = fluidTicks.map { SavedTick.UNIQUE_TICK_HASH.hashCode(it) }
            .hashCode()
        val entityHash = entities.hashCode()
        val persistentDataHash = persistentData.hashCode()
        val blockEntityHash = blockEntities.hashCode()
        val heightMapHash = heightMaps.mapValues { it.value.hash() }.hashCode()
        val sectionHash = sections.map { it?.hash() ?: 0 }.hashCode()
        return Objects.hash(blockTickHash, fluidTickHash, entityHash, persistentDataHash, blockEntityHash, heightMapHash, sectionHash)
    }

    override fun createSection(): AsyncChunkSection {
        val blocks = PalettedStorageImpl(object : PalettedStorageImplConfig<BlockState> {
            override val size: Int = 4096
            override val defaultState: BlockState = Blocks.AIR.defaultBlockState()

            override fun createStorage(bits: Int): NumberStorage {
                return BitStorage(size, bits)
            }

            override fun createCounter(bits: Int): TypeCounts {
                return IntCounts(bits, size)
            }

            override fun createPalette(): Palette<BlockState> {
                return SimplePalette(AW.globalBlockPalette)
            }

            override fun createIterationStrategy(): IterationStrategy {
                return NormalIteration(size)
            }

        })
        val biomes = PalettedStorageImpl(object : PalettedStorageImplConfig<Holder<Biome>> {
            override val size: Int = 64

            override val defaultState: Holder<Biome> = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(Biomes.PLAINS)

            override fun createStorage(bits: Int): NumberStorage {
                return BitStorage(size, bits)
            }

            override fun createCounter(bits: Int): TypeCounts {
                return IntCounts(bits, size)
            }

            override fun createPalette(): Palette<Holder<Biome>> {
                return SimplePalette(AW.globalBiomePalette)
            }

            override fun createIterationStrategy(): IterationStrategy {
                return NormalIteration(size)
            }

        })

        return BasicAsyncChunkSection(blocks, biomes)
    }
}