package net.ultragrav.kasyncworld.world.versionio

import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import org.bukkit.Chunk

interface ChunkIO {
    fun writeChunk(bukkitChunk: Chunk, chunk: AsyncChunk, options: ChunkWriteOptions)
    fun sendPackets(bukkitChunk: Chunk, chunk: AsyncChunk)
    fun readChunk(bukkitChunk: Chunk, factory: AsyncChunkFactory): AsyncChunk
}