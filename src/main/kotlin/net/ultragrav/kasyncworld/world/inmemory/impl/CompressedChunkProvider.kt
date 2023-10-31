package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.serializer.compressors.StandardCompressor
import java.nio.ByteBuffer

class CompressedChunkProvider(val factory: AsyncChunkFactory) : AsyncChunkProvider {

    val chunks = mutableMapOf<ChunkPos, ByteArray>()

    override fun loadChunk(x: Int, z: Int): AsyncChunk? {
        val array = StandardCompressor
            .instance
            .decompress(chunks[ChunkPos(x, z)] ?: return null)

        return AW.serializer.deserialize(ByteBuffer.wrap(array), factory)
    }

    override fun storeChunk(x: Int, z: Int, chunk: AsyncChunk) {
        val array = AW.serializer.serialize(chunk)
        chunks[ChunkPos(x, z)] = StandardCompressor
            .instance
            .compress(array)
    }

    override fun storeChunksCompressed(chunks: Map<ChunkPos, ByteArray>) {
        this.chunks.putAll(chunks)
    }
}