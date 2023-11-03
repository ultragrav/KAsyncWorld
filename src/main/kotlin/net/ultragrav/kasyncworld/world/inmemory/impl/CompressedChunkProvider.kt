package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.CompressedAsyncChunk
import net.ultragrav.kasyncworld.world.inmemory.SCompressedAsyncChunk

class CompressedChunkProvider(
    private val factory: AsyncChunkFactory,
    override val codec: ChunkCodec
) : AsyncChunkProvider {

    private val chunks = mutableMapOf<ChunkPos, CompressedAsyncChunk>()

    override fun loadChunk(x: Int, z: Int): AsyncChunk? {
        val compressed = chunks[ChunkPos(x, z)] ?: return null
        return compressed.decompress(factory)
    }

    override fun storeChunk(x: Int, z: Int, chunk: AsyncChunk) {
        val compressed = SCompressedAsyncChunk(chunk, codec)
        chunks[ChunkPos(x, z)] = compressed
    }

    override fun setChunks(chunkMap: Map<ChunkPos, CompressedAsyncChunk>) {
        chunks.clear()
        chunks.putAll(chunkMap)
    }

    override fun getChunks(): Map<ChunkPos, CompressedAsyncChunk> {
        return chunks
    }

}