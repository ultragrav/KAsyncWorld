package net.ultragrav.kasyncworld.world.chunk.queue

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.io.ChunkWriteOptions
import org.bukkit.World
import java.util.concurrent.CompletableFuture

interface ChunkQueue {
    fun start()
    fun stop()
    fun enqueue(
        x: Int,
        z: Int,
        world: World,
        chunk: AsyncChunk,
        writeOptions: ChunkWriteOptions
    ): CompletableFuture<Void>
}