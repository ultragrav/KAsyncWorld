package net.ultragrav.kasyncworld.world.contract

import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.heightmap.AWHeightMap
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSection
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSectionFactory
import net.ultragrav.nbt.wrapper.TagCompound
import org.bukkit.HeightMap
import org.bukkit.block.data.BlockData

/**
 * Represents a thread-safe representation of a chunk.
 */
interface AsyncChunk : AsyncChunkSectionFactory {

    val numSections: Int
    val minSectionY: Int
    val minBuildHeight: Int get() = minSectionY shl 4
    val maxBuildHeight: Int get() = height + minBuildHeight
    val height: Int get() = numSections shl 4

    fun setBlock(x: Int, y: Int, z: Int, block: BlockData)
    fun unsetBlock(x: Int, y: Int, z: Int)
    fun getBlock(x: Int, y: Int, z: Int): BlockData

    fun getHeightMap(type: HeightMap): AWHeightMap
    fun getHeightMaps(): Map<AWHeightMap.Type, AWHeightMap>
    fun setHeightMap(type: HeightMap, heightMap: AWHeightMap)
    fun clearHeightMaps()

    fun getTileEntity(x: Int, y: Int, z: Int): TagCompound?
    fun setTileEntity(x: Int, y: Int, z: Int, tag: TagCompound)
    fun removeTileEntity(x: Int, y: Int, z: Int)
    fun getTileEntities(): Map<AWBlockPosition, TagCompound>
    fun clearTileEntities()

    fun setSection(sectionIndex: Int, section: AsyncChunkSection)
    fun getSection(sectionIndex: Int): AsyncChunkSection

    /**
     * Returns an array of all sections in this chunk. The returned
     * array is a copy of the internal array, so modifying it will
     * not modify the chunk.
     */
    fun getSections(): Array<AsyncChunkSection>

    fun clearSections()

    fun getEntities(): List<TagCompound>
    fun addEntity(tag: TagCompound)
    fun removeEntity(tag: TagCompound)
    fun clearEntities()

    fun clone(): AsyncChunk
}