package net.ultragrav.kasyncworld.world

import net.ultragrav.kasyncworld.world.chunk.AsyncChunkSection
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage
import net.ultragrav.kasyncworld.world.chunk.heightmap.AWHeightMap
import net.ultragrav.nbt.wrapper.TagCompound
import org.bukkit.HeightMap
import org.bukkit.block.data.BlockData

interface AsyncChunk {

    val numSections: Int
    val minSectionY: Int
    val minBuildHeight: Int get() = minSectionY shl 4
    val maxBuildHeight: Int get() = height + minBuildHeight
    val height: Int get() = numSections shl 4

    val heightMaps: Map<AWHeightMap.Type, AWHeightMap>
    val sections: Array<AsyncChunkSection>
    val tileEntities: Map<AWBlockPosition, TagCompound>

    fun setBlock(x: Int, y: Int, z: Int, block: BlockData)
    fun getBlock(x: Int, y: Int, z: Int): BlockData
    fun getHeightMap(type: HeightMap)

    fun createSection(index: Int): AsyncChunkSection
}