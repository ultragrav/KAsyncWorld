package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.contract.AsyncChunk

interface InMemoryWorld {

    val name: String
    val options: InMemoryWorldOptions

    val chunkProvider: AsyncChunkProvider

    fun serialize(): ByteArray

    fun unload(save: Boolean)
}