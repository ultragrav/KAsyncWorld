package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory

interface CompressedAsyncChunk {
    val bytes: ByteArray
    val codec: ChunkCodec
    fun decompress(factory: AsyncChunkFactory): AsyncChunk
    fun clone(): CompressedAsyncChunk
}