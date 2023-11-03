package net.ultragrav.kasyncworld.world.inmemory

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk

interface AsyncChunkProvider {
    val codec: ChunkCodec
    fun loadChunk(x: Int, z: Int): AsyncChunk?
    fun storeChunk(x: Int, z: Int, chunk: AsyncChunk)
    fun setChunks(chunkMap: Map<ChunkPos, CompressedAsyncChunk>)
    fun getChunks(): Map<ChunkPos, CompressedAsyncChunk>
}