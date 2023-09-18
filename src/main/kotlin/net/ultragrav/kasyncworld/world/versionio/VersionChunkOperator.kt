package net.ultragrav.kasyncworld.world.versionio

import net.ultragrav.kasyncworld.world.AsyncChunk
import net.ultragrav.kasyncworld.world.AsyncChunkFactory
import org.bukkit.Chunk
import org.bukkit.World

interface VersionChunkOperator {
    fun writeChunk(bukkitChunk: Chunk, chunk: AsyncChunk, options: ChunkWriteOptions)
    fun readChunk(bukkitChunk: Chunk, factory: AsyncChunkFactory): AsyncChunk
}