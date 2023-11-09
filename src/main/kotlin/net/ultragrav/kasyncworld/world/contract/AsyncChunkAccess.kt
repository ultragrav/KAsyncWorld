package net.ultragrav.kasyncworld.world.contract

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.ultragrav.kasyncworld.world.chunk.heightmap.AsyncHeightMap

/**
 * Facilitates some sort of method of accessing and changing the data
 * in an [AsyncChunk] instance
 */
interface AsyncChunkAccess {
    fun setBlock(x: Int, y: Int, z: Int, block: BlockState)
    fun unsetBlock(x: Int, y: Int, z: Int)
    fun getBlock(x: Int, y: Int, z: Int): BlockState

    fun getHeightMap(type: Heightmap.Types): AsyncHeightMap
    fun setHeightMap(type: Heightmap.Types, heightMap: AsyncHeightMap)
    fun clearHeightMaps()

    fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag?
    fun setBlockEntity(x: Int, y: Int, z: Int, tag: CompoundTag)
    fun removeBlockEntity(x: Int, y: Int, z: Int)
    fun clearBlockEntities()

    fun addEntity(tag: CompoundTag)
    fun removeEntity(tag: CompoundTag)
    fun clearEntities()
}