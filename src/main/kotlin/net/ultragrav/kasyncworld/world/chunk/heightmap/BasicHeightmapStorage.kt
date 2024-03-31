package net.ultragrav.kasyncworld.world.chunk.heightmap

import net.ultragrav.kasyncworld.ceilLog2
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk

class BasicHeightmapStorage(val chunk: AsyncChunk) : HeightmapStorage {
    override val heightOptions = chunk.heightOptions

    private var data: NumberStorage = BitStorage(256, ceilLog2(chunk.heightOptions.maxBuildHeightExclusive + 1))

    override fun getHeight(x: Int, z: Int): Int {
        val index = (x and 0xF) shl 4 or (z and 0xF)
        return data.get(index) + heightOptions.minBuildHeightInclusive
    }

    override fun setHeight(x: Int, z: Int, height: Int) {
        val index = (x and 0xF) shl 4 or (z and 0xF)
        data.set(index, height - heightOptions.minBuildHeightInclusive)
    }

    override fun clone(): HeightmapStorage {
        val storage = BasicHeightmapStorage(chunk)
        storage.data = data.clone()
        return storage
    }

    override fun hash(): Int {
        return data.hashCode()
    }
}