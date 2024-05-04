package net.ultragrav.kasyncworld

import com.mojang.serialization.Compressable
import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.codec.LZ4WrappingCodec
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.inmemory.pack.PackedWorld
import net.ultragrav.kasyncworld.world.inmemory.IMWorldProvider
import net.ultragrav.kasyncworld.world.chunk.io.ChunkIO
import org.bukkit.World
import org.bukkit.plugin.Plugin

interface AWApi {
    fun initialize(plugin: Plugin)

    val chunkQueue: ChunkQueue
    val chunkIO: ChunkIO
    val codec: ChunkCodec
    val orderedCodec: ChunkCodec // Equal chunks -> Equal bytes
    val compressedCodec get() = LZ4WrappingCodec(codec)
    val compressedOrderedCodec get() = LZ4WrappingCodec(orderedCodec)
    val inMemoryWorldProvider: IMWorldProvider

    val editingChunkFactory: AsyncChunkFactory
    val storageChunkFactory: AsyncChunkFactory

    val defaultCodecResolver: (String) -> ChunkCodec
        get() = {
            when (it) {
                codec.id -> codec
                compressedCodec.id -> compressedCodec
                orderedCodec.id -> orderedCodec
                compressedOrderedCodec.id -> compressedOrderedCodec
                else -> throw IllegalArgumentException("Unknown codec id: $it")
            }
        }

    fun createAsyncWorld(world: World, editType: AsyncWorld.EditType): AsyncWorld
    fun createAsyncWorld(heightOptions: ChunkHeightOptions, editType: AsyncWorld.EditType): AsyncWorld

    fun createReader(bytes: ByteArray): DataReader
    fun createWriter(): DataWriter

    fun serializePackedWorld(packedWorld: PackedWorld): ByteArray
    fun deserializePackedWorld(data: ByteArray, codecProvider: (String) -> ChunkCodec = defaultCodecResolver): PackedWorld
}