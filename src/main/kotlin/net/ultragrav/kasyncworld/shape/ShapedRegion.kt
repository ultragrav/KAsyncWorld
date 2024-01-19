package net.ultragrav.kasyncworld.shape

import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition

interface ShapedRegion : Iterable<AWBlockPosition> {
    val boundingBox: CuboidRegion
    fun contains(x: Int, y: Int, z: Int): Boolean
}