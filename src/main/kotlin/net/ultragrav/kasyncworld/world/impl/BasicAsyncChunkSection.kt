package net.ultragrav.kasyncworld.world.impl

import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage
import net.ultragrav.kasyncworld.world.chunk.contract.section.AsyncChunkSection

class BasicAsyncChunkSection(
    override val blocks: PalettedStorage<BlockState>,
    override val biomes: PalettedStorage<Holder<Biome>>
) : AsyncChunkSection {
    override fun clone(): AsyncChunkSection {
        return BasicAsyncChunkSection(blocks.clone(), biomes.clone())
    }
}