package net.ultragrav.kasyncworld.world.versionio

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import org.bukkit.Chunk

interface ChunkIO {
    // May be run chunk-wise parallel
    fun writeChunk(bukkitChunk: Chunk, chunk: AsyncChunk, options: ChunkWriteOptions)
    fun sendPackets(bukkitChunk: Chunk, chunk: AsyncChunk)
    // May be run chunk-wise parallel
    fun readChunk(bukkitChunk: Chunk, factory: AsyncChunkFactory): AsyncChunk
}