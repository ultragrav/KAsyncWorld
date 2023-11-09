package net.ultragrav.kasyncworld.world.chunk.heightmap.wrapper

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunk
import net.ultragrav.kasyncworld.world.chunk.heightmap.HeightmapStateProvider
import java.util.function.Predicate

class NMSHeightmapStateProvider(val chunk: ChunkAccess) : HeightmapStateProvider {
    override fun getBlock(x: Int, y: Int, z: Int): BlockState {
        return chunk.getBlockState(x, y, z)
    }

    override fun canSkipLayer(y: Int, predicate: Predicate<BlockState>): Boolean {
        return false
    }
}