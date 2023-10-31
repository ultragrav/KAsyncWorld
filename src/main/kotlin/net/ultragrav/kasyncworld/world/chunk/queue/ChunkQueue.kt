package net.ultragrav.kasyncworld.world.chunk.queue

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.versionio.ChunkWriteOptions
import org.bukkit.World
import java.util.concurrent.CompletableFuture

interface ChunkQueue {
    fun enqueue(
        x: Int,
        z: Int,
        world: World,
        chunk: AsyncChunk,
        writeOptions: ChunkWriteOptions
    ): CompletableFuture<Void>
}