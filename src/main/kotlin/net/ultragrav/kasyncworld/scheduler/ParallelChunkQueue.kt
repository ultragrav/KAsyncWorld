package net.ultragrav.kasyncworld.scheduler

import kotlinx.coroutines.*
import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import net.ultragrav.kasyncworld.world.versionio.ChunkWriteOptions
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture


class ParallelChunkQueue(val plugin: Plugin, val io: ChunkIO) : ChunkQueue {

    val batchSize = 16

    private data class EnqueuedChunk(
        val x: Int,
        val z: Int,
        val world: World,
        val chunk: AsyncChunk,
        val writeOptions: ChunkWriteOptions,
        val future: CompletableFuture<Void> = CompletableFuture()
    )

    private val queue = mutableListOf<EnqueuedChunk>()

    private var taskId = -1

    fun start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::processNextBatch, 0, 1)
    }

    fun end() {
        if (taskId == -1) return
        Bukkit.getScheduler().cancelTask(taskId)
    }

    override fun enqueue(
        x: Int,
        z: Int,
        world: World,
        chunk: AsyncChunk,
        writeOptions: ChunkWriteOptions
    ): CompletableFuture<Void> {
        val enqueued = EnqueuedChunk(x, z, world, chunk, writeOptions)

        synchronized(this) {
            queue.add(enqueued)
        }

        return enqueued.future
    }

    /**
     * Gets the next batch of chunks to be written. This function will return a list
     * of the next enqueued chunks distinct in their x and z coordinates, and with a maximum
     * length of [batchSize].
     */
    private fun nextBatch(): List<EnqueuedChunk> {
        return synchronized(this) {
            val batch = queue.distinctBy { it.x to it.z }.take(batchSize)
            queue.removeAll(batch)
            batch
        }
    }

    /**
     * Processes the next batch of chunks.
     */
    private fun processNextBatch() {
        val batch = nextBatch()
        if (batch.isEmpty()) return

        runBlocking {
            batch.forEach { job ->
                val bukkitChunk = job.world.getChunkAt(job.x, job.z)
                val chunk = job.chunk
                launch {
                    withContext(Dispatchers.IO) {
                        io.writeChunk(bukkitChunk, chunk, job.writeOptions.copy(sendPackets = false))
                    }
                    io.sendPackets(bukkitChunk, chunk)
                }
            }
        }
        batch.forEach { it.future.complete(null) }
    }
}