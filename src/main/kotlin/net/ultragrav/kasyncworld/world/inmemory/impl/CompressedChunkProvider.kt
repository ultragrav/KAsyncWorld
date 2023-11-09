package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.CompressedAsyncChunk
import net.ultragrav.kasyncworld.world.inmemory.LocatedCompressedChunk
import net.ultragrav.kasyncworld.world.inmemory.SCompressedAsyncChunk

class CompressedChunkProvider(
    override val factory: AsyncChunkFactory,
    override val codec: ChunkCodec,
    override val boundsX: IntRange,
    override val boundsZ: IntRange
) : AsyncChunkProvider {

    private val chunks = mutableMapOf<ChunkPos, CompressedAsyncChunk>()

    override fun loadChunk(x: Int, z: Int): AsyncChunk? {
        if (x !in boundsX || z !in boundsZ) return null
        val compressed = chunks[ChunkPos(x, z)] ?: return null
        return compressed.decompress(factory)
    }

    override fun storeChunk(x: Int, z: Int, chunk: AsyncChunk) {
        if (x !in boundsX || z !in boundsZ) return
        val compressed = SCompressedAsyncChunk(chunk, codec)
        chunks[ChunkPos(x, z)] = compressed
    }

    override fun setChunks(chunkMap: Map<ChunkPos, CompressedAsyncChunk>) {
        chunks.clear()
        chunks.putAll(chunkMap.filterKeys { it.x in boundsX && it.z in boundsZ })
    }

    override fun getChunks(): Map<ChunkPos, CompressedAsyncChunk> {
        return chunks
    }

    override fun getLocatedChunks(): List<LocatedCompressedChunk> {
        return chunks.map { LocatedCompressedChunk(it.key.x, it.key.z, it.value) }
    }

}