package net.ultragrav.kasyncworld.shape

import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition

class CuboidRegion(p1: AWBlockPosition, p2: AWBlockPosition) : ShapedRegion {

    val min = AWBlockPosition(
        x = minOf(p1.x, p2.x),
        y = minOf(p1.y, p2.y),
        z = minOf(p1.z, p2.z)
    )

    val max = AWBlockPosition(
        x = maxOf(p1.x, p2.x),
        y = maxOf(p1.y, p2.y),
        z = maxOf(p1.z, p2.z)
    )

    override fun contains(x: Int, y: Int, z: Int): Boolean {
        return x in min.x..max.x && y in min.y..max.y && z in min.z..max.z
    }

    override fun intersects(region: CuboidRegion): Boolean {
        return region.min.x <= max.x && region.max.x >= min.x &&
                region.min.y <= max.y && region.max.y >= min.y &&
                region.min.z <= max.z && region.max.z >= min.z
    }
}