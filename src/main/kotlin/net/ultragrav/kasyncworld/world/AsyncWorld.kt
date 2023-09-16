package net.ultragrav.kasyncworld.world

import net.ultragrav.nbt.wrapper.TagCompound
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData
import org.bukkit.metadata.Metadatable

interface AsyncWorld {
    /**
     * Set the block at the given coordinates. If the block data provided is
     * a state that requires a tile entity, the tile entity will be set as well.
     */
    fun setBlock(x: Int, y: Int, z: Int, block: BlockData)

    /**
     * Set the tile entity at the given coordinates. If the block at the given
     * coordinates is not a tile entity, this method will throw an exception.
     * @throws IllegalStateException if the block at the given coordinates is not a tile entity
     */
    fun setTileEntity(x: Int, y: Int, z: Int, tile: TagCompound)

    /**
     * Unset the block at the given coordinates. If the block at the given
     * coordinates is a tile entity, the tile entity will be unset as well.
     */
    fun unsetBlock(x: Int, y: Int, z: Int)

    /**
     * Unset the tile entity at the given coordinates.
     */
    fun unsetTileEntity(x: Int, y: Int, z: Int)
}