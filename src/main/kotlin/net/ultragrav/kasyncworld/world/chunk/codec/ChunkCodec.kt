package net.ultragrav.kasyncworld.world.chunk.codec

import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import java.nio.ByteBuffer

interface ChunkCodec {
    val id: String
    val version: Int
    fun earlierVersion(): ChunkCodec?
    fun encode(writer: DataWriter, chunk: AsyncChunk)
    fun decode(reader: DataReader, factory: AsyncChunkFactory): AsyncChunk
}