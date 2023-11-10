package net.ultragrav.kasyncworld.world.chunk.block.palette

import net.minecraft.core.IdMap

class WrappedGlobalPalette<T>(val idList: IdMap<T>) : Palette<T> {
    override val size: Int
        get() = idList.size()

    override fun getState(id: Int): T {
        return idList.byIdOrThrow(id)
    }

    override fun isMapped(id: Int): Boolean {
        return idList.byId(id) != null
    }

    override fun listIds(): Set<Int> {
        return idList.map { idList.getId(it) }.toSet()
    }

    override fun globalPalette(): Palette<T> {
        return this
    }

    override fun clone(): Palette<T> {
        return WrappedGlobalPalette(idList)
    }

    override fun isMapped(type: T): Boolean {
        return idList.getId(type) != -1
    }

    override fun getId(type: T): Int {
        return idList.getId(type)
    }
}