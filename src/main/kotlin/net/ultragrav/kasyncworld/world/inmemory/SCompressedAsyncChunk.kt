package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.serializer.compressors.StandardCompressor
import java.nio.ByteBuffer

class SCompressedAsyncChunk(override val bytes: ByteArray, override val codec: ChunkCodec) : CompressedAsyncChunk {

    constructor(chunk: AsyncChunk, serializer: ChunkCodec)
            : this(StandardCompressor.instance.compress(serializer.encode(chunk)), serializer)

    override fun decompress(factory: AsyncChunkFactory): AsyncChunk {
        return codec.decode(
            ByteBuffer.wrap(StandardCompressor.instance.decompress(bytes)),
            factory
        )
    }

    override fun clone(): CompressedAsyncChunk {
        return SCompressedAsyncChunk(bytes.clone(), codec)
    }
}