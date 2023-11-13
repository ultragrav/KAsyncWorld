package net.ultragrav.kasyncworld.world.inmemory.pack

import net.ultragrav.kasyncworld.world.inmemory.chunk.EncodedAsyncChunk

data class PackedWorld(val chunks: List<EncodedAsyncChunk>)
