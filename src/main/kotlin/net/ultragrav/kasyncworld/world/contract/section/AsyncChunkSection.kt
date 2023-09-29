package net.ultragrav.kasyncworld.world.contract.section

import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage
import org.bukkit.block.Biome
import org.bukkit.block.data.BlockData

interface AsyncChunkSection {
    val blocks: PalettedStorage<BlockData>
    val biomes: PalettedStorage<Biome>

    fun clone(): AsyncChunkSection
}