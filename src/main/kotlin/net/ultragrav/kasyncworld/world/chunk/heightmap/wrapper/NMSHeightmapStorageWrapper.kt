package net.ultragrav.kasyncworld.world.chunk.heightmap.wrapper

import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.heightmap.HeightmapStorage

class NMSHeightmapStorageWrapper(val heightmap: Heightmap, val chunk: LevelChunk) : HeightmapStorage {
    override val heightOptions: ChunkHeightOptions = ChunkHeightOptions(
        numSections = chunk.sectionsCount,
        minSection = chunk.minSection
    )

    override fun getHeight(x: Int, z: Int): Int {
        return heightmap.getFirstAvailable(x, z)
    }

    override fun setHeight(x: Int, z: Int, height: Int) {
        heightmap.
    }

    override fun clone(): HeightmapStorage {
        TODO("Not yet implemented")
    }
}