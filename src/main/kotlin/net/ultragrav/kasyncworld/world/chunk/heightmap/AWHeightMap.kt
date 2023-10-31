package net.ultragrav.kasyncworld.world.chunk.heightmap

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.ultragrav.kasyncworld.ceilLog2
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import org.bukkit.HeightMap
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Waterlogged
import org.bukkit.block.data.type.Leaves
import java.util.function.Predicate

class AWHeightMap(val type: Heightmap.Types, val chunk: AsyncChunk) {

    val heightOptions = chunk.heightOptions

    private var data: NumberStorage = BitStorage(ceilLog2(heightOptions.maxBuildHeight + 1), 256)

    fun setHeight(x: Int, z: Int, height: Int) {
        data.set(x shl 4 or (z and 0xF), height - heightOptions.minBuildHeight)
    }

    fun getHeight(x: Int, z: Int): Int {
        return data.get(x shl 4 or (z and 0xF)) + heightOptions.minBuildHeight
    }

    fun clone(chunk: AsyncChunk = this.chunk): AWHeightMap {
        val map = AWHeightMap(type, chunk)
        map.data = data.clone()
        return map
    }

    fun recompute() {

        val maxHeight = heightOptions.maxBuildHeight
        val minHeight = heightOptions.minBuildHeight

        for (y in (maxHeight - 1) downTo minHeight) {
            for (x in 0..15) {
                for (z in 0..15) {
                    val block = chunk.getBlock(x, y, z)
                    if (type.isOpaque.test(block)) {
                        setHeight(x, z, y + 1)
                        break
                    }
                }
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

            for (j in (y - 1) downTo heightOptions.minBuildHeight) {
                val belowBlock = chunk.getBlock(x, j, z)

                // If we found an opaque block below:
                if (type.isOpaque.test(belowBlock)) {
                    setHeight(x, z, j + 1)
                    return true
                }
            }

            // If we didn't find any opaque blocks all the way down, set to minBuildHeight:
            setHeight(x, z, heightOptions.minBuildHeight)
            return true
        }

        return false
    }

}