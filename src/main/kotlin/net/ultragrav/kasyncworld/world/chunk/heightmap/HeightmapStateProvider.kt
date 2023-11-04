package net.ultragrav.kasyncworld.world.chunk.heightmap

import net.minecraft.world.level.block.state.BlockState
import java.util.function.Predicate

interface HeightmapStateProvider {
    fun getBlock(x: Int, y: Int, z: Int): BlockState
    fun canSkipLayer(y: Int, predicate: Predicate<BlockState>): Boolean
}