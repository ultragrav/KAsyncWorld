package net.ultragrav.kasyncworld.world.chunk.heightmap

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions

interface HeightmapStorage {
    val heightOptions: ChunkHeightOptions
    fun getHeight(x: Int, z: Int): Int
    fun setHeight(x: Int, z: Int, height: Int)
    fun clone(): HeightmapStorage
    fun hash(): Int
}