package net.ultragrav.kasyncworld.shape

import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.schematic.Dimensions

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

    val dimensions = Dimensions(
        x = max.x - min.x + 1,
        y = max.y - min.y + 1,
        z = max.z - min.z + 1
    )

    override val boundingBox = this

    override fun contains(x: Int, y: Int, z: Int): Boolean {
        return x in min.x..max.x && y in min.y..max.y && z in min.z..max.z
    }

    override fun iterator(): Iterator<AWBlockPosition> {
        return object : Iterator<AWBlockPosition> {
            var x = min.x
            var y = min.y
            var z = min.z

            override fun hasNext(): Boolean {
                return x <= max.x && y <= max.y && z <= max.z
            }

            override fun next(): AWBlockPosition {
                val pos = AWBlockPosition(x, y, z)
                if (++x > max.x) {
                    x = min.x
                    if (++y > max.y) {
                        y = min.y
                        ++z
                    }
                }
                return pos
            }
        }
    }
}