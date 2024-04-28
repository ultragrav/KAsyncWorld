package net.ultragrav.kasyncworld.world.schematic.mask

import net.minecraft.world.level.block.state.BlockState

interface Mask {
    fun shouldPlace(x: Int, y: Int, z: Int, existing: BlockState, new: BlockState): Boolean
}