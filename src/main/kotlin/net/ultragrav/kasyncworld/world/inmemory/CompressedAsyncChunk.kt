package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory

interface CompressedAsyncChunk {
    val bytes: ByteArray
    val codec: ChunkCodec
    fun decompress(factory: AsyncChunkFactory): AsyncChunk
    fun clone(): CompressedAsyncChunk
}