package net.ultragrav.kasyncworld.world.chunk.heightmap

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.ultragrav.kasyncworld.world.contract.AsyncChunk

class AsyncHeightMap(
    val type: Heightmap.Types,
    private val data: HeightmapStorage,
    private val stateProvider: HeightmapStateProvider
) {

    constructor(type: Heightmap.Types, chunk: AsyncChunk) :
            this(
                type,
                BasicHeightmapStorage(chunk),
                ChunkHeightmapStateProvider(chunk)
            )

    val heightOptions = data.heightOptions

    fun setHeight(x: Int, z: Int, height: Int) {
        data.setHeight(x, z, height)
    }

    fun getHeight(x: Int, z: Int): Int {
        return data.getHeight(x, z)
    }

    fun clone(stateProvider: HeightmapStateProvider = this.stateProvider): AsyncHeightMap {
        return AsyncHeightMap(type, data.clone(), stateProvider)
    }

    fun recompute() {
        for (x in 0..15) {
            for (z in 0..15) {
                recomputeColumn(x, z)
            }
        }
    }

    fun recomputeColumn(x: Int, z: Int, startFrom: Int = heightOptions.maxBuildHeightExclusive - 1) {
        val minHeight = heightOptions.minBuildHeightInclusive

        setHeight(x, z, minHeight)

        for (y in (startFrom - 1) downTo minHeight) {
            if (stateProvider.canSkipLayer(y, type.isOpaque)) continue

            val block = stateProvider.getBlock(x, y, z)
            if (type.isOpaque.test(block)) {
                setHeight(x, z, y + 1)
                break
            }
        }
    }

    fun update(x: Int, y: Int, z: Int, blockData: BlockState): Boolean {
        val currentHeight = getHeight(x, z)

        // If the new block is below the current topmost block minus 1, no update is required.
        if (y < currentHeight - 1) return false

        // If the blockData is opaque:
        if (type.isOpaque.test(blockData)) {
            // If the height of the new block is greater than or equal to the current height:
            if (y >= currentHeight) {
                setHeight(x, z, y + 1)
                return true
            }
        } else if (currentHeight - 1 == y) {
            // If the block data is not opaque and is right below the current height in the heightmap:

            for (j in (y - 1) downTo heightOptions.minBuildHeightInclusive) {
                val belowBlock = stateProvider.getBlock(x, j, z)

                // If we found an opaque block below:
                if (type.isOpaque.test(belowBlock)) {
                    setHeight(x, z, j + 1)
                    return true
                }
            }

            // If we didn't find any opaque blocks all the way down, set to minBuildHeight:
            setHeight(x, z, heightOptions.minBuildHeightInclusive)
            return true
        }

        return false
    }

    fun overwrite(other: AsyncHeightMap) {
        for (x in 0..15) {
            for (z in 0..15) {
                other.setHeight(x, z, getHeight(x, z))
            }
        }
    }

    fun editWith(other: AsyncHeightMap) {
        for (x in 0..15) {
            for (z in 0..15) {
                val edited = other.getHeight(x, z)
                val curr = getHeight(x, z)

                if (edited == curr) continue

                if (edited > curr) {
                    setHeight(x, z, edited)
                    continue
                }

                recomputeColumn(x, z, curr)

            }
        }
    }

}