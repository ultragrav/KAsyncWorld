package net.ultragrav.kasyncworld.world.chunk.impl

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory

class StorageChunkFactory : AsyncChunkFactory {
    override fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk {
        return StorageAsyncChunk(heightOptions)
    }
}