package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory

interface CompressedAsyncChunk {
    fun decompress(factory: AsyncChunkFactory): AsyncChunk
    fun clone(): CompressedAsyncChunk
}