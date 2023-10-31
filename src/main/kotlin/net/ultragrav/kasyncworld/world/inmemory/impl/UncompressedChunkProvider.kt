package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.serializer.compressors.StandardCompressor
import java.nio.ByteBuffer

class UncompressedChunkProvider(val factory: AsyncChunkFactory) : AsyncChunkProvider {

    val chunks = mutableMapOf<ChunkPos, AsyncChunk>()

    override fun loadChunk(x: Int, z: Int): AsyncChunk? {
        return chunks[ChunkPos(x, z)]
    }

    override fun storeChunk(x: Int, z: Int, chunk: AsyncChunk) {
        chunks[ChunkPos(x, z)] = chunk
    }

    override fun storeChunksCompressed(chunks: Map<ChunkPos, ByteArray>) {
        for ((pos, data) in chunks) {
            val decompressed = StandardCompressor.instance.decompress(data)
            this.chunks[pos] = AW.serializer.deserialize(ByteBuffer.wrap(decompressed), factory)
        }
    }
}