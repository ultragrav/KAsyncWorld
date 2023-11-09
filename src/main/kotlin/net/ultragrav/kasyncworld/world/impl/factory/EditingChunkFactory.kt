package net.ultragrav.kasyncworld.world.impl.factory

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.impl.EditingAsyncChunk

class EditingChunkFactory : AsyncChunkFactory {
    override fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk {
        return EditingAsyncChunk(heightOptions, AsyncWorld.EditType.SPARSE)
    }
}