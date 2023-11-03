package net.ultragrav.kasyncworld

import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import org.bukkit.plugin.Plugin

object AW : AWApi {
    override fun initialize(plugin: Plugin) {
        TODO("Not yet implemented")
    }

    override val chunkQueue: ChunkQueue
        get() = TODO("Not yet implemented")
    override val chunkIO: ChunkIO
        get() = TODO("Not yet implemented")
    override val serializer: ChunkCodec
        get() = TODO("Not yet implemented")
}