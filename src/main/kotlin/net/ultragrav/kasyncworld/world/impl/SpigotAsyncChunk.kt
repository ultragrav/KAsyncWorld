package net.ultragrav.kasyncworld.world.impl

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.ticks.SavedTick
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.heightmap.AWHeightMap
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSection

class SpigotAsyncChunk(
    override val heightOptions: ChunkHeightOptions
) : AsyncChunk {

    override val sections: Array<AsyncChunkSection?> = arrayOfNulls(heightOptions.numSections)
    override val heightMaps: MutableMap<Heightmap.Types, AWHeightMap> = mutableMapOf()
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

    override fun getHeightMap(type: Heightmap.Types): AWHeightMap {
        return heightMaps.getOrPut(type) { AWHeightMap(type, this) }
    }

    override fun setHeightMap(type: Heightmap.Types, heightMap: AWHeightMap) {
        if (heightMap.heightOptions != this.heightOptions) {
            throw IllegalArgumentException("Height map has different height options")
        }

        heightMaps[type] = heightMap.clone(chunk = this)
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
        val copy = SpigotAsyncChunk(heightOptions)
        copy.entities.addAll(entities)
        copy.blockEntities.putAll(blockEntities)
        copy.heightMaps.putAll(heightMaps)
        copy.sections.forEachIndexed { index, section ->
            copy.sections[index] = section?.clone()
        }
        return copy
    }

    override fun createSection(): AsyncChunkSection {
        TODO("Not yet implemented")
    }
}
