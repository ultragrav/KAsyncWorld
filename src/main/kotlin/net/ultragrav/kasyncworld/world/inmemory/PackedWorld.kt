package net.ultragrav.kasyncworld.world.inmemory

data class LocatedCompressedChunk(val x: Int, val z: Int, val chunk: CompressedAsyncChunk)
data class PackedWorld(val chunks: List<LocatedCompressedChunk>)
