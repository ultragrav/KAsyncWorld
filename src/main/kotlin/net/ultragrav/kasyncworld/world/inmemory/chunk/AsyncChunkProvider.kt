package net.ultragrav.kasyncworld.world.inmemory.chunk

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory

interface AsyncChunkProvider {
    val codec: ChunkCodec
    val factory: AsyncChunkFactory
    val boundsX: IntRange
    val boundsZ: IntRange
    fun loadChunk(x: Int, z: Int): AsyncChunk?
    fun storeChunk(x: Int, z: Int, chunk: AsyncChunk)
    fun setChunks(chunkMap: List<EncodedAsyncChunk>)
    fun getChunks(): List<EncodedAsyncChunk>
}