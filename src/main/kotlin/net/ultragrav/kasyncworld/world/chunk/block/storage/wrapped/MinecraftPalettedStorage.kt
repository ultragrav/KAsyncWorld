package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.minecraft.world.level.chunk.PalettedContainer
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage

interface MinecraftPalettedStorage<T> : PalettedStorage<T> {
    val wrapped: PalettedContainer<T>
}