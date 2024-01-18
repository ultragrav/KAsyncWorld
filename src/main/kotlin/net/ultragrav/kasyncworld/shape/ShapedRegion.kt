package net.ultragrav.kasyncworld.shape

import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition

interface ShapedRegion {
    operator fun contains(pos: AWBlockPosition): Boolean = contains(pos.x, pos.y, pos.z)
    fun contains(x: Int, y: Int, z: Int): Boolean
    fun intersects(region: CuboidRegion): Boolean
}