package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.stdCompress
import net.ultragrav.kasyncworld.stdDecompress
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import java.util.*

class SCompressedAsyncChunk(override val bytes: ByteArray, override val codec: ChunkCodec) : CompressedAsyncChunk {

    constructor(chunk: AsyncChunk, codec: ChunkCodec)
            : this(toOtherConstructor(chunk, codec), codec)

    override fun decompress(factory: AsyncChunkFactory): AsyncChunk {
        return codec.decode(
            AW.createReader(stdDecompress(bytes)),
            factory
        )
    }

    override fun clone(): CompressedAsyncChunk {
        return SCompressedAsyncChunk(bytes.copyOf(), codec)
    }

    companion object {
        private fun toOtherConstructor(chunk: AsyncChunk, codec: ChunkCodec): ByteArray {
            val writer = AW.createWriter()
            codec.encode(writer, chunk)
            return stdCompress(writer.toByteArray())
        }
    }
}