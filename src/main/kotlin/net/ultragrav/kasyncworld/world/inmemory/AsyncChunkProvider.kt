package net.ultragrav.kasyncworld.world.inmemory

import net.minecraft.world.level.ChunkPos
import net.ultragrav.kasyncworld.world.contract.AsyncChunk

interface AsyncChunkProvider {
    fun loadChunk(x: Int, z: Int): AsyncChunk?
    fun storeChunk(x: Int, z: Int, chunk: AsyncChunk)
    fun storeChunksCompressed(chunks: Map<ChunkPos, ByteArray>)
}