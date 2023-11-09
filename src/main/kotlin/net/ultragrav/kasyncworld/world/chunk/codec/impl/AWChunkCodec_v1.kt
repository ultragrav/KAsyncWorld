package net.ultragrav.kasyncworld.world.chunk.codec.impl

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import java.nio.ByteBuffer

class AWChunkCodec_v1 : ChunkCodec {
    override val id: String
        get() = "aw"
    override val version: Int
        get() = 1

    override fun earlierVersion() = null

    override fun encode(chunk: AsyncChunk): ByteArray {
        return ByteArray(0)
    }

    override fun decode(data: ByteBuffer, factory: AsyncChunkFactory): AsyncChunk {
        return factory.createChunk(ChunkHeightOptions(20, -4))
    }

}