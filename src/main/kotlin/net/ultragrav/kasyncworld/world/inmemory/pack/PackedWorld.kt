package net.ultragrav.kasyncworld.world.inmemory.pack

import net.ultragrav.kasyncworld.world.inmemory.chunk.CompressedAsyncChunk

data class LocatedCompressedChunk(val x: Int, val z: Int, val chunk: CompressedAsyncChunk)
data class PackedWorld(val chunks: List<LocatedCompressedChunk>)
