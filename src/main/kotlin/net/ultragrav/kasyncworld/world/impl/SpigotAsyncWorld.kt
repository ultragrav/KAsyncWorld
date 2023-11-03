package net.ultragrav.kasyncworld.world.impl

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.getChunkKey
import net.ultragrav.kasyncworld.getChunkX
import net.ultragrav.kasyncworld.getChunkZ
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.versionio.ChunkWriteOptions
import org.bukkit.World
import java.util.concurrent.CompletableFuture

internal class SpigotAsyncWorld internal constructor(val world: World) : AsyncWorld {

    private val chunkMap = mutableMapOf<Long, AsyncChunk>()

    override fun setBlock(x: Int, y: Int, z: Int, block: BlockState) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setBlock(x and 15, y, z and 15, block)
    }

    override fun setBlockEntity(x: Int, y: Int, z: Int, tile: CompoundTag) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setBlockEntity(x and 15, y, z and 15, tile)
    }

    override fun unsetBlock(x: Int, y: Int, z: Int) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.unsetBlock(x and 15, y, z and 15)
    }

    override fun unsetBlockEntity(x: Int, y: Int, z: Int) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.removeBlockEntity(x and 15, y, z and 15)
    }

    override fun setChunk(cx: Int, cz: Int, chunk: AsyncChunk) {
        synchronized(this) {
            chunkMap[getChunkKey(cx, cz)] = chunk
        }
    }

    override fun getChunk(cx: Int, cz: Int): AsyncChunk {
        val key = getChunkKey(cx, cz)
        return synchronized(this) {
            val worldHeight = world.maxHeight - world.minHeight
            check(worldHeight and 0xF == 0) { "World height must be a multiple of 16" }
            chunkMap.getOrPut(key) {
                val chunk = createChunk(
                    ChunkHeightOptions(
                        numSections = worldHeight shr 4,
                        minSection = world.minHeight shr 4
                    )
                )
                chunkMap[key] = chunk
                chunk
            }
        }
    }

    override fun flush(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }

    override fun syncFlush() {
        val io = AW.chunkIO

        val chunks = synchronized(this) {
            val copy = chunkMap.toMap()
            chunkMap.clear()
            copy
        }

        val writeOptions = ChunkWriteOptions(
            appendEntities = true,
        )

        chunks.forEach { (key, chunk) ->
            val cx = getChunkX(key)
            val cz = getChunkZ(key)
            val bukkitChunk = world.getChunkAt(cx, cz)
            io.writeChunk(bukkitChunk, chunk, writeOptions)
        }
    }

    override fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk {
        return SpigotAsyncChunk(heightOptions)
    }
}