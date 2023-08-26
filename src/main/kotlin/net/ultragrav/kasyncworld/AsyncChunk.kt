package net.ultragrav.kasyncworld

import net.minecraft.core.BlockPosition
import net.minecraft.world.level.block.state.IBlockData

class AsyncChunk internal constructor(val world: AsyncWorld, val x: Int, val z: Int) {
    internal var handle = world.handle.getChunkIfLoaded(x, z)

    fun getBlock(x: Int, y: Int, z: Int): IBlockData {
        val sectionIndex = y shr 4 // TODO: Subtract minY
        val palettedContainer = handle.d()[sectionIndex].h()
    }
}