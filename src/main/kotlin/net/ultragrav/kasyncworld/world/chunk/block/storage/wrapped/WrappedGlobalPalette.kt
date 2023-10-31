package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.minecraft.core.IdMap
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette

class WrappedGlobalPalette<T>(val global: IdMap<T>) : Palette<T> {
    override val size: Int
        get() = global.size()

    override fun getState(id: Int): T {
        return global.byIdOrThrow(id)
    }

    override fun isMapped(id: Int): Boolean {
        return global.byId(id) != null
    }

    override fun globalPalette(): Palette<T> {
        return this
    }

    override fun clone(): Palette<T> {
        return WrappedGlobalPalette(global)
    }

    override fun isMapped(type: T): Boolean {
        return global.getId(type) != IdMap.DEFAULT
    }

    override fun getId(type: T): Int {
        return global.getId(type)
    }
}