package net.ultragrav.kasyncworld.world.impl

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.ticks.SavedTick
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.IntCounts
import net.ultragrav.kasyncworld.world.chunk.block.count.TypeCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.LinkedChangeIteration
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.iteration.FlagChangeIteration
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.palette.SimplePalette
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImpl
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImplConfig
import net.ultragrav.kasyncworld.world.chunk.heightmap.AsyncHeightMap
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSection

class SpigotAsyncChunk(
    override val heightOptions: ChunkHeightOptions,
    val editType: AsyncWorld.EditType
) : AsyncChunk {

    override val sections: Array<AsyncChunkSection?> = arrayOfNulls(heightOptions.numSections)
    override val heightMaps: MutableMap<Heightmap.Types, AsyncHeightMap> = mutableMapOf()
    override val blockEntities: MutableMap<AWBlockPosition, CompoundTag> = mutableMapOf()
    override val entities: MutableList<CompoundTag> = mutableListOf()

    override var blockTicks: MutableList<SavedTick<Block>> = mutableListOf()
    override var fluidTicks: MutableList<SavedTick<Fluid>> = mutableListOf()

    override var persistentData: CompoundTag = CompoundTag()

    override fun setBlock(x: Int, y: Int, z: Int, block: BlockState) {
        val section = getOrMakeSection(y shr 4)
        section.setBlock(x, y and 15, z, block)
    }

    override fun unsetBlock(x: Int, y: Int, z: Int) {
        val section = getOrMakeSection(y shr 4)
        section.unsetBlock(x, y and 15, z)
    }

    override fun getBlock(x: Int, y: Int, z: Int): BlockState {
        val section = getSection((y shr 4))
            ?: return Blocks.AIR.defaultBlockState()
        return section.getBlock(x, y and 15, z)
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
        val copy = SpigotAsyncChunk(heightOptions, editType)
        copy.entities.addAll(entities)
        copy.blockTicks.addAll(blockTicks)
        copy.fluidTicks.addAll(fluidTicks)
        copy.persistentData = persistentData.copy()
        copy.blockEntities.putAll(blockEntities)
        copy.heightMaps.putAll(heightMaps)
        copy.sections.forEachIndexed { index, section ->
            copy.sections[index] = section?.clone()
        }
        return copy
    }

    val sparseBlockConfig = object : PalettedStorageImplConfig<BlockState> {
        override val size = 4096
        override val defaultState = Blocks.AIR.defaultBlockState()

        override fun createStorage(bits: Int): NumberStorage {
            return BitStorage(size, bits)
        }

        override fun createCounter(bits: Int): TypeCounts {
            return IntCounts(bits, size)
        }

        override fun createPalette(): Palette<BlockState> {
            return SimplePalette()
        }

        override fun createIterationStrategy(): IterationStrategy {
            return LinkedChangeIteration(size)
        }

    }

    val sparseBiomeConfig = object : PalettedStorageImplConfig<Holder<Biome>> {
        override val size = 64

        override val defaultState = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.PLAINS)

        override fun createStorage(bits: Int): NumberStorage {
            return BitStorage(size, bits)
        }

        override fun createCounter(bits: Int): TypeCounts {
            return IntCounts(bits, size)
        }

        override fun createPalette(): Palette<Holder<Biome>> {
            return SimplePalette()
        }

        override fun createIterationStrategy(): IterationStrategy {
            return LinkedChangeIteration(size)
        }

    }

    val denseBlockConfig = object : PalettedStorageImplConfig<BlockState> {
        override val size = 4096
        override val defaultState = Blocks.AIR.defaultBlockState()

        override fun createStorage(bits: Int): NumberStorage {
            return BitStorage(size, bits)
        }

        override fun createCounter(bits: Int): TypeCounts {
            return IntCounts(bits, size)
        }

        override fun createPalette(): Palette<BlockState> {
            return SimplePalette()
        }

        override fun createIterationStrategy(): IterationStrategy {
            return FlagChangeIteration(size)
        }

    }

    val denseBiomeConfig = object : PalettedStorageImplConfig<Holder<Biome>> {
        override val size = 64

        override val defaultState = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(Biomes.PLAINS)

        override fun createStorage(bits: Int): NumberStorage {
            return BitStorage(size, bits)
        }

        override fun createCounter(bits: Int): TypeCounts {
            return IntCounts(bits, size)
        }

        override fun createPalette(): Palette<Holder<Biome>> {
            return SimplePalette()
        }

        override fun createIterationStrategy(): IterationStrategy {
            return FlagChangeIteration(size)
        }

    }

    override fun createSection(): AsyncChunkSection {
        return when (editType) {
            AsyncWorld.EditType.SPARSE, AsyncWorld.EditType.MIXED -> {
                val blocks = PalettedStorageImpl(sparseBlockConfig)
                val biomes = PalettedStorageImpl(sparseBiomeConfig)
                BasicAsyncChunkSection(blocks, biomes)
            }

            AsyncWorld.EditType.DENSE -> {
                val blocks = PalettedStorageImpl(denseBlockConfig)
                val biomes = PalettedStorageImpl(denseBiomeConfig)
                BasicAsyncChunkSection(blocks, biomes)
            }
        }
    }
}
