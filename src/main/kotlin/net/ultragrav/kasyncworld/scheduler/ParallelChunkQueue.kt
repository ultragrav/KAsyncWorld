package net.ultragrav.kasyncworld.scheduler

import kotlinx.coroutines.*
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.io.ChunkIO
import net.ultragrav.kasyncworld.world.chunk.io.ChunkWriteOptions
import net.ultragrav.kasyncworld.world.chunk.queue.ChunkQueue
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.time.measureTimedValue


class ParallelChunkQueue(val plugin: Plugin, val io: ChunkIO) : ChunkQueue {

    private val processors = Runtime.getRuntime().availableProcessors()
    private val batchSize = processors * 2
    private val dispatcher = AW.parallelDispatcher

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

    override fun start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::process, 0, 1)
    }

    override fun close() {
        if (taskId == -1) return
        Bukkit.getScheduler().cancelTask(taskId)
        (dispatcher as? ExecutorCoroutineDispatcher)?.close()
    }

    fun isEmpty(): Boolean {
        return synchronized(this) { queue.isEmpty() }
    }

    fun isNotEmpty(): Boolean {
        return synchronized(this) { queue.isNotEmpty() }
    }

    fun process() {
        val time = System.currentTimeMillis()
        fun elapsed() = System.currentTimeMillis() - time
        fun isNotEmpty() = synchronized(this) { queue.isNotEmpty() }
        if (!isNotEmpty()) return

        while (elapsed() < 35 && isNotEmpty()) {

            val (processedCount, timingMillis) = measureTimedValue {
                processNextBatch()
            }

            AW.debug("Processed batch of $processedCount chunks in $timingMillis")
        }
        AW.debug("Finished processing for this tick in ${elapsed()}ms")
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
    private fun processNextBatch(): Int {
        val batch = nextBatch()
        if (batch.isEmpty()) return 0

        runBlocking {
            batch.forEach { job ->
                val bukkitChunk = job.world.getChunkAt(job.x, job.z)
                val chunk = job.chunk
                launch {
                    withContext(dispatcher) {
                        io.writeChunk(bukkitChunk, chunk, job.writeOptions)
                    }
                }
            }
        }

        batch.forEach { it.future.complete(null) }

        return batch.size
    }
}