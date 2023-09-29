package net.ultragrav.kasyncworld.world.contract

/**
 * Facilitates the creation of an AsyncChunk.
 */
interface AsyncChunkFactory {
    fun createChunk(numSections: Int, minSectionY: Int): AsyncChunk
}