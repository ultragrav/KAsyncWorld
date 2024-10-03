package net.ultragrav.kasyncworld.world.contract

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.shape.ShapedRegion
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.block.data.CraftBlockData
import java.util.concurrent.CompletableFuture

interface AsyncWorld : AsyncChunkFactory {

    val heightOptions: ChunkHeightOptions

    /**
     * Set the block at the given coordinates. If the block data provided is
     * a state that requires a tile entity, the tile entity will be set as well.
     */
    fun setBlock(x: Int, y: Int, z: Int, block: BlockState)

    fun setBlockData(x: Int, y: Int, z: Int, blockData: BlockData) = setBlock(x, y, z, (blockData as CraftBlockData).state)

    fun chunks(): Set<AsyncChunk>

    /**
     * Set the tile entity at the given coordinates. If the block at the given
     * coordinates is not a tile entity, this method will throw an exception.
     * @throws IllegalStateException if the block at the given coordinates is not a tile entity
     */
    fun setBlockEntity(x: Int, y: Int, z: Int, tile: CompoundTag)

    fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag?

    /**
     * Unset the block at the given coordinates. If the block at the given
     * coordinates is a tile entity, the tile entity will be unset as well.
     */
    fun unsetBlock(x: Int, y: Int, z: Int)

    /**
     * Unset the tile entity at the given coordinates.
     */
    fun unsetBlockEntity(x: Int, y: Int, z: Int)

    /**
     * Replace the chunk at the given chunk-coordinates with the given chunk.
     */
    fun setChunk(cx: Int, cz: Int, chunk: AsyncChunk)

    /**
     * Get the chunk at the given chunk-coordinates. If the chunk does not exist then
     * it will be created. The returned chunks are associated with this async world
     * and should not be used or modified after either [flush] or [syncFlush] is called.
     */
    fun getChunk(cx: Int, cz: Int): AsyncChunk

    fun getBlock(x: Int, y: Int, z: Int): BlockState

    fun getBlockData(x: Int, y: Int, z: Int): BlockData = getBlock(x, y, z).let { CraftBlockData.fromData(it) }

    fun setRegion(region: ShapedRegion, block: BlockState) {
        region.forEach { (x, y, z) -> setBlock(x, y, z, block) }
    }

    /**
     * Pushes all changes to the world. This method returns a future that completes
     * when all current changes have been pushed to the world.
     *
     * The chunk instances in this world should not be modified after this method is called,
     * the current chunk instances will be cleared, so it is safe to modify chunks newly obtained from
     * this world after this method is called. However, it is not safe to modify chunks obtained
     * from this world before this method is called.
     */
    fun flush(): CompletableFuture<Void>

    /**
     * Immediately pushes all changes to the world. This method is not safe to
     * use on any thread other than the main thread.
     */
    fun syncFlush()

    fun createChunk() = createChunk(heightOptions)

    fun clone(): AsyncWorld

    enum class EditType {
        DENSE, SPARSE, MIXED
    }
}