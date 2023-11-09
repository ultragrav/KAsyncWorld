package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.CompressedAsyncChunk
import net.ultragrav.kasyncworld.world.inmemory.LocatedCompressedChunk
import net.ultragrav.kasyncworld.world.inmemory.SCompressedAsyncChunk

class UncompressedChunkProvider(
    override val factory: AsyncChunkFactory,
    override val codec: ChunkCodec,
    override val boundsX: IntRange,
    override val boundsZ: IntRange
) : AsyncChunkProvider {

    private val chunks = mutableMapOf<ChunkPos, AsyncChunk>()

    override fun loadChunk(x: Int, z: Int): AsyncChunk? {
        if (x !in boundsX || z !in boundsZ) return null
        return chunks[ChunkPos(x, z)]
    }

    override fun storeChunk(x: Int, z: Int, chunk: AsyncChunk) {
        if (x !in boundsX || z !in boundsZ) return
        chunks[ChunkPos(x, z)] = chunk
    }

    override fun setChunks(chunkMap: Map<ChunkPos, CompressedAsyncChunk>) {
        chunks.clear()
        for ((pos, compressed) in chunkMap) {
            if (pos.x !in boundsX || pos.z !in boundsZ) continue
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

    override fun getLocatedChunks(): List<LocatedCompressedChunk> {
        return chunks.map { LocatedCompressedChunk(it.key.x, it.key.z, SCompressedAsyncChunk(it.value, codec)) }
    }
}