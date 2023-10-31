package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.minecraft.core.IdMap
import net.minecraft.world.level.chunk.MissingPaletteEntryException
import net.minecraft.world.level.chunk.PalettedContainer
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette

internal class WrappedPalette<T>(val global: IdMap<T>, val palette: net.minecraft.world.level.chunk.Palette<T>) : Palette<T> {
    override val size: Int
        get() = palette.size

    override fun getState(id: Int): T {
        return palette.valueFor(id)
    }

    override fun isMapped(id: Int): Boolean {
        return try {
            palette.valueFor(id)
            true
        } catch (e: MissingPaletteEntryException) {
            false
        }
    }

    override fun isMapped(type: T): Boolean {
        return palette.maybeHas { it == type }
    }

    override fun globalPalette(): Palette<T> {
        return WrappedGlobalPalette(global)
    }

    override fun clone(): Palette<T> {
        return WrappedPalette(global, palette.copy())
    }

    override fun getId(type: T): Int {
        return palette.idFor(type)
    }
}