package net.ultragrav.kasyncworld

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.data.GravSerializerRead
import net.ultragrav.kasyncworld.data.GravSerializerWrite
import net.ultragrav.kasyncworld.scheduler.ParallelChunkQueue
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.palette.WrappedGlobalPalette
import net.ultragrav.kasyncworld.world.chunk.codec.ChunkCodec
import net.ultragrav.kasyncworld.world.chunk.codec.impl.AWChunkCodecV0
import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.impl.SpigotAsyncWorld
import net.ultragrav.kasyncworld.world.impl.factory.EditingChunkFactory
import net.ultragrav.kasyncworld.world.impl.factory.StorageChunkFactory
import net.ultragrav.kasyncworld.world.inmemory.IMWorldProvider
import net.ultragrav.kasyncworld.world.inmemory.LocatedCompressedChunk
import net.ultragrav.kasyncworld.world.inmemory.PackedWorld
import net.ultragrav.kasyncworld.world.inmemory.SCompressedAsyncChunk
import net.ultragrav.kasyncworld.world.inmemory.impl.PaperIMWorldProvider
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import net.ultragrav.kasyncworld.world.versionio.impl.NMSChunkIO
import net.ultragrav.kserializer.json.JsonArray
import net.ultragrav.kserializer.json.JsonObject
import net.ultragrav.serializer.GravSerializer
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture

object AW : AWApi {

    val debug = true

    override val chunkIO: ChunkIO = NMSChunkIO()

    override lateinit var chunkQueue: ChunkQueue

    override val codec: ChunkCodec = AWChunkCodecV0()

    override val inMemoryWorldProvider: IMWorldProvider = PaperIMWorldProvider()

    override val editingChunkFactory: AsyncChunkFactory = EditingChunkFactory()
    override val storageChunkFactory: AsyncChunkFactory = StorageChunkFactory()

    internal val globalBlockPalette: Palette<BlockState> =
        WrappedGlobalPalette(Block.BLOCK_STATE_REGISTRY)

    internal val globalBiomePalette: Palette<Holder<Biome>> =
        WrappedGlobalPalette(MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME).asHolderIdMap())

    override fun initialize(plugin: Plugin) {
        chunkQueue = ParallelChunkQueue(plugin, chunkIO)
        chunkQueue.start()
    }

    override fun createAsyncWorld(world: World, editType: AsyncWorld.EditType): AsyncWorld {
        return SpigotAsyncWorld(world, editType)
    }

    override fun createReader(bytes: ByteArray): DataReader {
        if (bytes.isEmpty()) return GravSerializerRead(GravSerializer(bytes))
        val firstByte = bytes[0] // Will be 0 if GravSerializer
        require(firstByte.toInt() == 0) { "Haven't implemented non-grav serializer writer yet!" }
        val ser = GravSerializer(bytes)
        ser.readByte() // Skip first byte
        return GravSerializerRead(ser)
    }

    override fun createWriter(): DataWriter {
        val writer = GravSerializerWrite(GravSerializer())
        writer.writeByte(0) // Write first byte as 0 to indicate grav serializer
        return writer
    }

    fun editAsync(world: World, editType: AsyncWorld.EditType, job: AsyncWorld.() -> Unit): CompletableFuture<Void> {
        val asyncWorld = createAsyncWorld(world, editType)
        val scope = CoroutineScope(Dispatchers.IO)
        val future = CompletableFuture<Void>()
        scope.launch {
            job(asyncWorld)
            asyncWorld.flush().thenAccept { future.complete(null) }
        }
        return future
    }

    inline fun editSync(world: World, editType: AsyncWorld.EditType, job: AsyncWorld.() -> Unit) {
        require(Bukkit.isPrimaryThread()) { "Cannot use editSync on an asynchronous thread!" }
        val asyncWorld = createAsyncWorld(world, editType)
        job(asyncWorld)
        asyncWorld.syncFlush()
    }

    override fun serializePackedWorld(packedWorld: PackedWorld): ByteArray {
        val json = JsonObject()
        val array = JsonArray()
        for (chunk in packedWorld.chunks) {
            val chunkJson = JsonObject()
            chunkJson["x"] = chunk.x
            chunkJson["z"] = chunk.z
            val codec = chunk.chunk.codec
            chunkJson["codec"] = codec.id
            chunkJson["version"] = codec.version
            chunkJson["data"] = chunk.chunk.bytes
            array.add(chunkJson)
        }
        json["chunks"] = array
        return json.toByteArray()
    }

    override fun deserializePackedWorld(data: ByteArray, codec: ChunkCodec): PackedWorld {
        val json = JsonObject.deserialize(GravSerializer(data))

        val chunks = mutableListOf<LocatedCompressedChunk>()
        val array = json.getArray("chunks")
        val size = array.size
        for (i in 0 until size) {
            val chunkJson = array.getObject(i)
            val x = chunkJson.getNumber("x").toInt()
            val z = chunkJson.getNumber("z").toInt()
            val isCorrectCodec = chunkJson.getString("codec") == codec.id
            if (!isCorrectCodec) throw IllegalArgumentException("Codec mismatch: ${chunkJson.getString("codec")} != ${codec.id}")

            val version = chunkJson.getNumber("version").toInt()
            val chunkBytes = chunkJson.getBinary("data").value

            if (version > codec.version) throw IllegalArgumentException("Version mismatch: $version > ${codec.version}")

            var currentCodec = codec
            while (version < currentCodec.version) currentCodec = currentCodec.earlierVersion()
                ?: throw IllegalArgumentException("Cannot find codec for version $version")
            require(version == currentCodec.version) { "Could not find version $version of codec ${currentCodec.id}" }

            val compressedChunk = SCompressedAsyncChunk(chunkBytes, currentCodec)
            chunks.add(LocatedCompressedChunk(x, z, compressedChunk))
        }

        return PackedWorld(chunks)
    }

    internal fun debug(msg: String) {
        if (!debug) return
        Bukkit.getLogger().info("[AW Debug] $msg")
    }
}

fun World.editSync(editType: AsyncWorld.EditType, job: AsyncWorld.() -> Unit) {
    AW.editSync(this, editType, job)
}

fun World.editAsync(editType: AsyncWorld.EditType, job: AsyncWorld.() -> Unit): CompletableFuture<Void> {
    return AW.editAsync(this, editType, job)
}

fun JsonObject.toByteArray(): ByteArray {
    val ser = GravSerializer()
    this.serialize(ser)
    return ser.toByteArray()
}
