package net.ultragrav.kasyncworld.world.chunk.serialization

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import java.nio.ByteBuffer

interface ChunkSerializer {
    fun serialize(chunk: AsyncChunk): ByteArray
    fun deserialize(data: ByteBuffer, factory: AsyncChunkFactory): AsyncChunk
}