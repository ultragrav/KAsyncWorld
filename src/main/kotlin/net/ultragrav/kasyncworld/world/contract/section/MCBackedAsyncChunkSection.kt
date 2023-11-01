package net.ultragrav.kasyncworld.world.contract.section

import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped.MinecraftPalettedStorage

interface MCBackedAsyncChunkSection : AsyncChunkSection {
    override val blocks: MinecraftPalettedStorage<BlockState>
    override val biomes: MinecraftPalettedStorage<Holder<Biome>>
}