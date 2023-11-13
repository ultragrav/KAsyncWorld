package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.chunk.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.chunk.EncodedAsyncChunk

class BasicChunkProvider(
    override val factory: AsyncChunkFactory,
    override val codec: ChunkCodec,
    override val boundsX: IntRange,
    override val boundsZ: IntRange
) : AsyncChunkProvider {

    private val chunks = mutableMapOf<ChunkPos, EncodedAsyncChunk>()

    override fun loadChunk(x: Int, z: Int): AsyncChunk? {
        if (x !in boundsX || z !in boundsZ) return null
        return chunks[ChunkPos(x, z)]?.expand(factory)
    }

    override fun storeChunk(x: Int, z: Int, chunk: AsyncChunk) {
        if (x !in boundsX || z !in boundsZ) return
        chunks[ChunkPos(x, z)] = EncodedAsyncChunk.fromChunk(codec, chunk, x, z)
    }

    override fun setChunks(chunkMap: List<EncodedAsyncChunk>) {
        chunks.clear()
        for (chunk in chunkMap) {
            chunks[ChunkPos(chunk.x, chunk.z)] = chunk
        }
    }

    override fun getChunks(): List<EncodedAsyncChunk> {
        return chunks.values.toList()
    }
}