package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.CompressedAsyncChunk
import net.ultragrav.kasyncworld.world.inmemory.SCompressedAsyncChunk
import net.ultragrav.serializer.compressors.StandardCompressor
import java.nio.ByteBuffer

class UncompressedChunkProvider(
    private val factory: AsyncChunkFactory,
    override val codec: ChunkCodec
) : AsyncChunkProvider {

    private val chunks = mutableMapOf<ChunkPos, AsyncChunk>()

    override fun loadChunk(x: Int, z: Int): AsyncChunk? {
        return chunks[ChunkPos(x, z)]
    }

    override fun storeChunk(x: Int, z: Int, chunk: AsyncChunk) {
        chunks[ChunkPos(x, z)] = chunk
    }

    override fun setChunks(chunkMap: Map<ChunkPos, CompressedAsyncChunk>) {
        chunks.clear()
        for ((pos, compressed) in chunkMap) {
            chunks[pos] = compressed.decompress(factory)
        }
    }

    override fun getChunks(): Map<ChunkPos, CompressedAsyncChunk> {
        val map = mutableMapOf<ChunkPos, CompressedAsyncChunk>()
        for ((pos, chunk) in chunks) {
            map[pos] = SCompressedAsyncChunk(chunk, codec)
        }
        return map
    }
}