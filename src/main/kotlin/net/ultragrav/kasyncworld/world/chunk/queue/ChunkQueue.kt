package net.ultragrav.kasyncworld.world.chunk.queue

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.versionio.ChunkWriteOptions
import java.util.concurrent.CompletableFuture

interface ChunkQueue {
    fun enqueue(
        x: Int,
        z: Int,
        chunk: AsyncChunk,
        writeOptions: ChunkWriteOptions
    ): CompletableFuture<Void>
}