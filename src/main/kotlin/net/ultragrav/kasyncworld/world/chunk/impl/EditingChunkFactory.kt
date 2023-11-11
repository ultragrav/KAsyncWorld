package net.ultragrav.kasyncworld.world.chunk.impl

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.contract.AsyncWorld

class EditingChunkFactory : AsyncChunkFactory {
    override fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk {
        return EditingAsyncChunk(heightOptions, AsyncWorld.EditType.SPARSE)
    }
}