package net.ultragrav.kasyncworld

import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.impl.SpigotAsyncChunk
import net.ultragrav.kasyncworld.world.impl.SpigotAsyncWorld
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import org.bukkit.World
import org.bukkit.plugin.Plugin

interface AWApi {
    fun initialize(plugin: Plugin)
    val chunkQueue: ChunkQueue
    val chunkIO: ChunkIO
    val codec: ChunkCodec

    fun createAsyncWorld(world: World, editType: AsyncWorld.EditType): AsyncWorld
}