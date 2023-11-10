package net.ultragrav.kasyncworld

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.chunk.Palette
import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
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

    fun createReader(bytes: ByteArray): DataReader
    fun createWriter(): DataWriter

    fun serializePackedWorld(packedWorld: PackedWorld): ByteArray
    fun deserializePackedWorld(data: ByteArray, codec: ChunkCodec = this.codec): PackedWorld
}