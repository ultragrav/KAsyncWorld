package net.ultragrav.kasyncworld.world.contract

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions

/**
 * Facilitates the creation of an AsyncChunk.
 */
interface AsyncChunkFactory {
    fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk
}