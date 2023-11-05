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
import net.ultragrav.kasyncworld.world.versionio.HeightmapWriteType
import org.bukkit.World
import java.util.concurrent.CompletableFuture

internal class SpigotAsyncWorld internal constructor(val world: World, val editType: AsyncWorld.EditType) : AsyncWorld {

    private val chunkMap = mutableMapOf<Long, AsyncChunk>()

    override val heightOptions = ChunkHeightOptions(
        (world.maxHeight - world.minHeight) shr 4,
        world.minHeight shr 4,
    )

    init {
        require(world.minHeight % 16 == 0) { "World min height must be a multiple of 16" }
        require(world.maxHeight % 16 == 0) { "World max height must be a multiple of 16" }
    }

    override fun setBlock(x: Int, y: Int, z: Int, block: BlockState) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setBlock(x and 15, y.coerceIn(heightOptions.buildableYRange), z and 15, block)
    }

    override fun setBlockEntity(x: Int, y: Int, z: Int, tile: CompoundTag) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setBlockEntity(x and 15, y.coerceIn(heightOptions.buildableYRange), z and 15, tile)
    }

    override fun unsetBlock(x: Int, y: Int, z: Int) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.unsetBlock(x and 15, y.coerceIn(heightOptions.buildableYRange), z and 15)
    }

    override fun unsetBlockEntity(x: Int, y: Int, z: Int) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.removeBlockEntity(x and 15, y.coerceIn(heightOptions.buildableYRange), z and 15)
    }

    override fun setChunk(cx: Int, cz: Int, chunk: AsyncChunk) {
        chunkMap[getChunkKey(cx, cz)] = chunk
    }

    override fun getChunk(cx: Int, cz: Int): AsyncChunk {
        val key = getChunkKey(cx, cz)
        return chunkMap.getOrPut(key) {
            val chunk = createChunk()
            chunkMap[key] = chunk
            chunk
        }
    }

    override fun flush(): CompletableFuture<Void> {
        val chunks = chunkMap.toMap()
        chunkMap.clear()

        val writeOptions = ChunkWriteOptions(
            appendEntities = true,
            heightmapWriteType = HeightmapWriteType.MERGE,
            writePersistentContainer = false
        )

        return CompletableFuture.allOf(
            *chunks.map { (key, chunk) ->
                val cx = getChunkX(key)
                val cz = getChunkZ(key)
                AW.chunkQueue.enqueue(cx, cz, world, chunk, writeOptions)
            }.toTypedArray()
        )
    }

    override fun syncFlush() {
        val io = AW.chunkIO

        val chunks = chunkMap.toMap()
        chunkMap.clear()

        val writeOptions = ChunkWriteOptions(
            appendEntities = true,
            heightmapWriteType = HeightmapWriteType.MERGE,
            writePersistentContainer = false
        )

        chunks.forEach { (key, chunk) ->
            val cx = getChunkX(key)
            val cz = getChunkZ(key)
            val bukkitChunk = world.getChunkAt(cx, cz)
            io.writeChunk(bukkitChunk, chunk, writeOptions)
        }
    }

    override fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk {
        return SpigotAsyncChunk(heightOptions, editType)
    }

}