package net.ultragrav.kasyncworld.world.inmemory.pack

import net.ultragrav.kasyncworld.world.inmemory.chunk.EncodedAsyncChunk

data class PackedWorld(val chunks: List<EncodedAsyncChunk>) {
    val chunkMinX get() = chunks.minOf { it.x }
    val chunkMinZ get() = chunks.minOf { it.z }
    val chunkMaxX get() = chunks.maxOf { it.x }
    val chunkMaxZ get() = chunks.maxOf { it.z }

    val chunkRangeX get() = chunkMinX..chunkMaxX
    val chunkRangeZ get() = chunkMinZ..chunkMaxZ
}
