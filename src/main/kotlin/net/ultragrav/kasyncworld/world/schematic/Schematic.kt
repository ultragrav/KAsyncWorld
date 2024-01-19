package net.ultragrav.kasyncworld.world.schematic

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.shape.ShapedRegion
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.block.position.PositionedBlock
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import org.bukkit.block.data.BlockData

interface Schematic : Iterable<PositionedBlock> {
    val dimensions: Dimensions
    fun setBlock(x: Int, y: Int, z: Int, block: BlockState)
    fun setBlockData(x: Int, y: Int, z: Int, block: BlockData)
    fun getBlock(x: Int, y: Int, z: Int): BlockState
    fun getBlockData(x: Int, y: Int, z: Int): BlockData
    fun unsetBlock(x: Int, y: Int, z: Int)
    fun setBlockEntity(x: Int, y: Int, z: Int, tag: CompoundTag)
    fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag?
    fun removeBlockEntity(x: Int, y: Int, z: Int)
    fun blockEntities(): Map<AWBlockPosition, CompoundTag>
    fun entities(): List<CompoundTag>
    fun types(): Set<BlockState>
    fun typesData(): Set<BlockData>
    fun contains(state: BlockState): Boolean = state in types()
    fun contains(blockData: BlockData): Boolean = blockData in typesData()
    fun clone(): Schematic
}