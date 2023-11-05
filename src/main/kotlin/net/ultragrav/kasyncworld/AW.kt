package net.ultragrav.kasyncworld

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ultragrav.kasyncworld.scheduler.ParallelChunkQueue
import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.chunk.serialization.ChunkCodec
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.impl.SpigotAsyncWorld
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import net.ultragrav.kasyncworld.world.versionio.impl.NMSChunkIO
import org.bukkit.World
import org.bukkit.plugin.Plugin
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

object AW : AWApi {


    override val chunkIO = NMSChunkIO()

    override lateinit var chunkQueue: ChunkQueue
    override val codec: ChunkCodec
        get() = object : ChunkCodec {
            override fun encode(chunk: AsyncChunk): ByteArray {
                throw UnsupportedOperationException("Cannot encode chunk")
            }

            override fun decode(data: ByteBuffer, factory: AsyncChunkFactory): AsyncChunk {
                throw UnsupportedOperationException("Cannot decode chunk")
            }
        }


    override fun initialize(plugin: Plugin) {
        chunkQueue = ParallelChunkQueue(plugin, chunkIO)
        chunkQueue.start()
    }

    override fun createAsyncWorld(world: World, editType: AsyncWorld.EditType): AsyncWorld {
        return SpigotAsyncWorld(world, editType)
    }

    fun editAsync(world: World, editType: AsyncWorld.EditType, job: AsyncWorld.() -> Unit): CompletableFuture<Void> {
        val asyncWorld = createAsyncWorld(world, editType)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch { job(asyncWorld) }
        return asyncWorld.flush()
    }

    inline fun editSync(world: World, editType: AsyncWorld.EditType, job: AsyncWorld.() -> Unit) {
        val asyncWorld = createAsyncWorld(world, editType)
        job(asyncWorld)
        asyncWorld.syncFlush()
    }
}