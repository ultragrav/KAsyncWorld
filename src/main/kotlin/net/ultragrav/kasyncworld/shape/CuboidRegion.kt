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

    constructor(dimensions: Dimensions) : this(
        AWBlockPosition(0, 0, 0),
        AWBlockPosition(dimensions.x - 1, dimensions.y - 1, dimensions.z - 1)
    )

    override val boundingBox = this

    override fun contains(x: Int, y: Int, z: Int): Boolean {
        return x in min.x..max.x && y in min.y..max.y && z in min.z..max.z
    }

    operator fun plus(pos: AWBlockPosition): CuboidRegion {
        return CuboidRegion(
            AWBlockPosition(min.x + pos.x, min.y + pos.y, min.z + pos.z),
            AWBlockPosition(max.x + pos.x, max.y + pos.y, max.z + pos.z)
        )
    }

    operator fun minus(pos: AWBlockPosition): CuboidRegion {
        return CuboidRegion(
            AWBlockPosition(min.x - pos.x, min.y - pos.y, min.z - pos.z),
            AWBlockPosition(max.x - pos.x, max.y - pos.y, max.z - pos.z)
        )
    }

    fun chunks(): List<Pair<Int, Int>> {
        val minChunkX = min.x shr 4
        val minChunkZ = min.z shr 4
        val maxChunkX = max.x shr 4
        val maxChunkZ = max.z shr 4
        val chunks = mutableListOf<Pair<Int, Int>>()
        for (x in minChunkX..maxChunkX) {
            for (z in minChunkZ..maxChunkZ) {
                chunks.add(x to z)
            }
        }
        return chunks
    }

    fun intersection(other: CuboidRegion): CuboidRegion {
        val minX = maxOf(min.x, other.min.x)
        val minY = maxOf(min.y, other.min.y)
        val minZ = maxOf(min.z, other.min.z)
        val maxX = minOf(max.x, other.max.x)
        val maxY = minOf(max.y, other.max.y)
        val maxZ = minOf(max.z, other.max.z)
        return if (minX <= maxX && minY <= maxY && minZ <= maxZ) {
            CuboidRegion(AWBlockPosition(minX, minY, minZ), AWBlockPosition(maxX, maxY, maxZ))
        } else {
            CuboidRegion(AWBlockPosition(0, 0, 0), AWBlockPosition(-1, -1, -1))
        }
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
                if (++y > max.y) {
                    y = min.y
                    if (++z > max.z) {
                        z = min.z
                        x++
                    }
                }
                return pos
            }
        }
    }
}