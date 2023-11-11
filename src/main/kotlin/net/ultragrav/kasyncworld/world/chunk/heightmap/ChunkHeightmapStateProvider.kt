package net.ultragrav.kasyncworld.world.chunk.heightmap

import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import java.util.function.Predicate

class ChunkHeightmapStateProvider(private val chunk: AsyncChunk) : HeightmapStateProvider {
    override fun getBlock(x: Int, y: Int, z: Int): BlockState {
        return chunk.getBlock(x, y, z)
    }

    override fun canSkipLayer(y: Int, predicate: Predicate<BlockState>): Boolean {
        val sectionIndexMB = y shr 4
        val section = chunk.getSection(sectionIndexMB) ?: return true
        val blocks = section.blocks
        if (!blocks.fastCountsAndTypesSupported) return false
        return blocks.types().none { predicate.test(it) }
    }
}