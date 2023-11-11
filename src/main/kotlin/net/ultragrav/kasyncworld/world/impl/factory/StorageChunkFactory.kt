package net.ultragrav.kasyncworld.world.impl.factory

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.impl.StorageAsyncChunk

class StorageChunkFactory : AsyncChunkFactory {
    override fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk {
        return StorageAsyncChunk(heightOptions)
    }
}