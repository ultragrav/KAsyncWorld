package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.minecraft.world.level.chunk.PalettedContainer
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.storage.Indexed
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage

class WrappedPalettedContainer<T>(
    override val wrapped: PalettedContainer<T>
) : MinecraftPalettedStorage<T> {

    override val storage: NumberStorage
        get() = WrappedBitStorage(wrapped.data.storage)
    override val palette: Palette<T>
        get() =

    override fun types(): Set<T> {

    }

    override fun get(index: Int): T {
        TODO("Not yet implemented")
    }

    override fun unset(index: Int) {
        TODO("Not yet implemented")
    }

    override fun clone(): PalettedStorage<T> {
        TODO("Not yet implemented")
    }

    override fun indexIterator(): Iterator<Int> {
        TODO("Not yet implemented")
    }

    override fun contains(type: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, type: T) {
        TODO("Not yet implemented")
    }

    override fun count(type: T): Int {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<Indexed<T>> {
        TODO("Not yet implemented")
    }

}