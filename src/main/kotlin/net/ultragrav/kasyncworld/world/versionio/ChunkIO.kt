package net.ultragrav.kasyncworld.world.versionio

import net.minecraft.world.level.chunk.LevelChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import org.bukkit.Chunk
import org.bukkit.World

interface ChunkIO {
    // May be run chunk-wise parallel
    fun writeChunk(nms: LevelChunk, chunk: AsyncChunk, options: ChunkWriteOptions)
    fun sendPackets(world: World, cx: Int, cz: Int)
    // May be run chunk-wise parallel
    fun readChunk(nms: LevelChunk, factory: AsyncChunkFactory, options: ChunkReadOptions): AsyncChunk
}