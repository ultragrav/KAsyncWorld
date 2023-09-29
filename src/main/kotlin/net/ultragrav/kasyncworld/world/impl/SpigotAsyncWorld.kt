package net.ultragrav.kasyncworld.world.impl

import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.getChunkKey
import net.ultragrav.kasyncworld.getChunkX
import net.ultragrav.kasyncworld.getChunkZ
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.versionio.ChunkWriteOptions
import net.ultragrav.nbt.wrapper.TagCompound
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import java.util.concurrent.CompletableFuture

internal class SpigotAsyncWorld internal constructor(val world: World) : AsyncWorld {

    private val chunkMap = mutableMapOf<Long, AsyncChunk>()

    override fun setBlock(x: Int, y: Int, z: Int, block: BlockData) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setBlock(x and 15, y, z and 15, block)
    }

    override fun setTileEntity(x: Int, y: Int, z: Int, tile: TagCompound) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setTileEntity(x and 15, y, z and 15, tile)
    }

    override fun unsetBlock(x: Int, y: Int, z: Int) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.unsetBlock(x and 15, y, z and 15)
    }

    override fun unsetTileEntity(x: Int, y: Int, z: Int) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.removeTileEntity(x and 15, y, z and 15)
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
                    worldHeight shr 4,
                    world.minHeight shr 4
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
            ignoreEmptySections = true,
            appendTiles = true,
            appendEntities = true,
        )

        chunks.forEach { (key, chunk) ->
            val cx = getChunkX(key)
            val cz = getChunkZ(key)
            val bukkitChunk = world.getChunkAt(cx, cz)
            io.writeChunk(bukkitChunk, chunk, writeOptions)
        }
    }

    override fun createChunk(numSections: Int, minSectionY: Int): AsyncChunk {
        return SpigotAsyncChunk(numSections, minSectionY)
    }
}