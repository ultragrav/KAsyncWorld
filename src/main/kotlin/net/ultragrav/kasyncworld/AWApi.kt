package net.ultragrav.kasyncworld

import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.inmemory.PackedWorld
import net.ultragrav.kasyncworld.world.inmemory.IMWorldProvider
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import org.bukkit.World
import org.bukkit.plugin.Plugin

interface AWApi {
    fun initialize(plugin: Plugin)
    val chunkQueue: ChunkQueue
    val chunkIO: ChunkIO
    val codec: ChunkCodec
    val inMemoryWorldProvider: IMWorldProvider

    val editingChunkFactory: AsyncChunkFactory
    val storageChunkFactory: AsyncChunkFactory

    fun createAsyncWorld(world: World, editType: AsyncWorld.EditType): AsyncWorld

    fun serializePackedWorld(packedWorld: PackedWorld): ByteArray
    fun deserializePackedWorld(data: ByteArray, codec: ChunkCodec = this.codec): PackedWorld
}