package net.ultragrav.kasyncworld.world.chunk.block.position

import net.minecraft.world.level.block.state.BlockState

data class PositionedBlock(val x: Int, val y: Int, val z: Int, val state: BlockState)