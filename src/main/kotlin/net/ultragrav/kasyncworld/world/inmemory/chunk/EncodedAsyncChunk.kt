package net.ultragrav.kasyncworld.world.inmemory.chunk

import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory

class EncodedAsyncChunk(
    val x: Int,
    val z: Int,
    val data: ByteArray,
    val codec: ChunkCodec
) {

    fun expand(factory: AsyncChunkFactory): AsyncChunk {
        val reader = AW.createReader(data)
        return codec.decode(reader, factory)
    }

    companion object {
        fun fromChunk(codec: ChunkCodec, chunk: AsyncChunk, x: Int, z: Int): EncodedAsyncChunk {
            val writer = AW.createWriter()
            codec.encode(writer, chunk)
            return EncodedAsyncChunk(x, z, writer.toByteArray(), codec)
        }
    }
}