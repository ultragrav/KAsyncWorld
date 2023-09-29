package net.ultragrav.kasyncworld.world.impl

import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.heightmap.AWHeightMap
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSection
import net.ultragrav.nbt.wrapper.TagCompound
import org.bukkit.HeightMap
import org.bukkit.block.data.BlockData

class SpigotAsyncChunk(
    override val numSections: Int,
    override val minSectionY: Int
) : AsyncChunk {
    override fun setBlock(x: Int, y: Int, z: Int, block: BlockData) {
        TODO("Not yet implemented")
    }

    override fun getBlock(x: Int, y: Int, z: Int): BlockData {
        TODO("Not yet implemented")
    }

    override fun getHeightMap(type: HeightMap): AWHeightMap {
        TODO("Not yet implemented")
    }

    override fun getHeightMaps(): Map<AWHeightMap.Type, AWHeightMap> {
        TODO("Not yet implemented")
    }

    override fun setHeightMap(type: HeightMap, heightMap: AWHeightMap) {
        TODO("Not yet implemented")
    }

    override fun clearHeightMaps() {
        TODO("Not yet implemented")
    }

    override fun getTileEntity(x: Int, y: Int, z: Int): TagCompound? {
        TODO("Not yet implemented")
    }

    override fun setTileEntity(x: Int, y: Int, z: Int, tag: TagCompound?) {
        TODO("Not yet implemented")
    }

    override fun getTileEntities(): Map<AWBlockPosition, TagCompound> {
        TODO("Not yet implemented")
    }

    override fun clearTileEntities() {
        TODO("Not yet implemented")
    }

    override fun setSection(sectionIndex: Int, section: AsyncChunkSection) {
        TODO("Not yet implemented")
    }

    override fun getSection(sectionIndex: Int): AsyncChunkSection {
        TODO("Not yet implemented")
    }

    override fun getSections(): Array<AsyncChunkSection> {
        TODO("Not yet implemented")
    }

    override fun clearSections() {
        TODO("Not yet implemented")
    }

    override fun getEntities(): List<TagCompound> {
        TODO("Not yet implemented")
    }

    override fun addEntity(tag: TagCompound) {
        TODO("Not yet implemented")
    }

    override fun removeEntity(tag: TagCompound) {
        TODO("Not yet implemented")
    }

    override fun clearEntities() {
        TODO("Not yet implemented")
    }

    override fun clone(): AsyncChunk {
        TODO("Not yet implemented")
    }

    override fun createSection(): AsyncChunkSection {
        TODO("Not yet implemented")
    }
}