package net.ultragrav.kasyncworld.world.inmemory

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import org.bukkit.World.Environment

data class InMemoryWorldOptions(
    val environment: Environment,
    val sizeChunksX: Int,
    val sizeChunksY: Int,
    val heightOptions: ChunkHeightOptions,
    val compressUnloadedChunks: Boolean,
    val codec: ChunkCodec
)