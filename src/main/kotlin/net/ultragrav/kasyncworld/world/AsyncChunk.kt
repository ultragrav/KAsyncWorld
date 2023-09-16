package net.ultragrav.kasyncworld.world

import org.bukkit.block.data.BlockData

interface AsyncChunk {

    val numSections: Int
    val minSectionY: Int
    val minBuildHeight: Int get() = minSectionY shl 4
    val height: Int get() = numSections shl 4

    fun setBlock(x: Int, y: Int, z: Int, block: BlockData)
    fun getBlock(x: Int, y: Int, z: Int): BlockData
}