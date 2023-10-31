package net.ultragrav.kasyncworld.world.inmemory.impl

import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorld
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions

class PaperMemoryWorld(
    override val name: String,
    override val options: InMemoryWorldOptions,
    chunkFactory: AsyncChunkFactory
) : InMemoryWorld {

    override val chunkProvider =
        if (options.compressUnloadedChunks) CompressedChunkProvider(chunkFactory)
        else UncompressedChunkProvider(chunkFactory)

    override fun serialize(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun unload(save: Boolean) {
        TODO("Not yet implemented")
    }
}