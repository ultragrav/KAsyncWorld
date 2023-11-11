package net.ultragrav.kasyncworld.world.chunk.io

import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import org.bukkit.Chunk
import org.bukkit.World

interface ChunkIO {
    // May be run chunk-wise parallel
    fun writeChunk(bukkitChunk: Chunk, chunk: AsyncChunk, options: ChunkWriteOptions)
    fun sendPackets(world: World, cx: Int, cz: Int)
    // May be run chunk-wise parallel
    fun readChunk(bukkitChunk: Chunk, factory: AsyncChunkFactory, options: ChunkReadOptions): AsyncChunk
}