package net.ultragrav.kasyncworld.world.impl

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.getChunkKey
import net.ultragrav.kasyncworld.getChunkX
import net.ultragrav.kasyncworld.getChunkZ
import net.ultragrav.kasyncworld.scheduler.ParallelChunkQueue
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.impl.EditingAsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.chunk.io.ChunkWriteOptions
import net.ultragrav.kasyncworld.world.chunk.io.HeightmapWriteType
import org.bukkit.World
import java.util.concurrent.CompletableFuture

internal class SpigotAsyncWorld private constructor(val world: World?, override val heightOptions: ChunkHeightOptions, val editType: AsyncWorld.EditType) : AsyncWorld {

    private val chunkMap = mutableMapOf<Long, AsyncChunk>()

    constructor(world: World, editType: AsyncWorld.EditType) : this(world, ChunkHeightOptions(
        (world.maxHeight - world.minHeight) shr 4,
        world.minHeight shr 4,
    ), editType)

    constructor(heightOptions: ChunkHeightOptions, editType: AsyncWorld.EditType) : this(null, heightOptions, editType)

    init {
        if (world != null) {
            require(world.minHeight % 16 == 0) { "World min height must be a multiple of 16" }
            require(world.maxHeight % 16 == 0) { "World max height must be a multiple of 16" }
        }
    }

    override fun chunks(): Set<AsyncChunk> {
        return chunkMap.values.toSet()
    }

    override fun setBlock(x: Int, y: Int, z: Int, block: BlockState) {
        if (y !in heightOptions.buildableYRange) return
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setBlock(x and 15, y, z and 15, block)
    }

    override fun setBlockEntity(x: Int, y: Int, z: Int, tile: CompoundTag) {
        if (y !in heightOptions.buildableYRange) return
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.setBlockEntity(x and 15, y, z and 15, tile)
    }

    override fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag? {
        if (y !in heightOptions.buildableYRange) return null
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        return chunk.getBlockEntity(x and 15, y, z and 15)
    }

    override fun unsetBlock(x: Int, y: Int, z: Int) {
        if (y !in heightOptions.buildableYRange) return
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.unsetBlock(x and 15, y, z and 15)
    }

    override fun unsetBlockEntity(x: Int, y: Int, z: Int) {
        if (y !in heightOptions.buildableYRange) return
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        chunk.removeBlockEntity(x and 15, y, z and 15)
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

    override fun getBlock(x: Int, y: Int, z: Int): BlockState {
        if (y !in heightOptions.buildableYRange) return Blocks.AIR.defaultBlockState()
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val chunk = getChunk(chunkX, chunkZ)
        return chunk.getBlock(x and 15, y, z and 15)
    }

    override fun flush(): CompletableFuture<Void> {

        require(world != null) { "Cannot flush a world without a Bukkit world" }

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

        require(world != null) { "Cannot flush a world without a Bukkit world" }

        val io = AW.chunkIO

        val chunks = chunkMap.toMap()
        chunkMap.clear()

        val writeOptions = ChunkWriteOptions(
            appendEntities = true,
            heightmapWriteType = HeightmapWriteType.MERGE,
            writePersistentContainer = false
        )

        val queue = AW.chunkQueue

        if (queue is ParallelChunkQueue && chunks.size > 8) {
            val newQueue = ParallelChunkQueue(queue.plugin, queue.io)
            chunks.forEach { (key, chunk) ->
                val cx = getChunkX(key)
                val cz = getChunkZ(key)
                newQueue.enqueue(cx, cz, world, chunk, writeOptions)
            }
            while (newQueue.isNotEmpty()) newQueue.process()
            // Do not close as the dispatcher is tied to another queue
        } else {
            chunks.forEach { (key, chunk) ->
                val cx = getChunkX(key)
                val cz = getChunkZ(key)
                val bukkitChunk = world.getChunkAt(cx, cz)
                io.writeChunk(bukkitChunk, chunk, writeOptions)
            }
        }
    }

    override fun clone(): AsyncWorld {
        val newWorld = SpigotAsyncWorld(world, heightOptions, editType)
        chunkMap.forEach { (key, chunk) ->
            val cx = getChunkX(key)
            val cz = getChunkZ(key)
            newWorld.setChunk(cx, cz, chunk.clone())
        }
        return newWorld
    }

    override fun createChunk(heightOptions: ChunkHeightOptions): AsyncChunk {
        return EditingAsyncChunk(heightOptions, editType)
    }

}