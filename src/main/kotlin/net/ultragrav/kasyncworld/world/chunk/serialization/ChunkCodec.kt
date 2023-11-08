package net.ultragrav.kasyncworld.world.chunk.serialization

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import java.nio.ByteBuffer

interface ChunkCodec {
    val id: String
    val version: Int
    fun earlierVersion(): ChunkCodec?
    fun encode(chunk: AsyncChunk): ByteArray
    fun decode(data: ByteBuffer, factory: AsyncChunkFactory): AsyncChunk
}