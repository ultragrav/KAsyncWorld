package net.ultragrav.kasyncworld

import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import org.bukkit.plugin.Plugin

interface AWApi {
    fun initialize(plugin: Plugin)
    val chunkQueue: ChunkQueue
    val chunkIO: ChunkIO
    val serializer: ChunkCodec
}