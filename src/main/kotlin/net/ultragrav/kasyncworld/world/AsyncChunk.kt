package net.ultragrav.kasyncworld.world

import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage
import net.ultragrav.kasyncworld.world.chunk.heightmap.AWHeightMap
import org.bukkit.HeightMap
import org.bukkit.block.data.BlockData

interface AsyncChunk {

    val numSections: Int
    val minSectionY: Int
    val minBuildHeight: Int get() = minSectionY shl 4
    val maxBuildHeight: Int get() = height + minBuildHeight
    val height: Int get() = numSections shl 4

    val heightMaps: Map<AWHeightMap.Type, AWHeightMap>
    val sections: Array<PalettedStorage>

    fun setBlock(x: Int, y: Int, z: Int, block: BlockData)
    fun getBlock(x: Int, y: Int, z: Int): BlockData
    fun getHeightMap(type: HeightMap)
}