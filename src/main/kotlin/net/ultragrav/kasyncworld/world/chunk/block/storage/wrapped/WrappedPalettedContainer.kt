package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.minecraft.world.level.chunk.PalettedContainer
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.storage.Indexed
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage
import java.util.concurrent.atomic.AtomicInteger

class WrappedPalettedContainer<T>(
    override val wrapped: PalettedContainer<T>
) : MinecraftPalettedStorage<T> {

    override val storage: NumberStorage
        get() = WrappedBitStorage(wrapped.data.storage)

    override val palette: Palette<T>
        get() = WrappedPalette(wrapped.registry, wrapped.data.palette)

    override fun types(): Set<T> {
        return (0 until wrapped.data.palette.size)
            .map { wrapped.data.palette.valueFor(it) }
            .toSet()
    }

    override fun get(index: Int): T {
        return wrapped[index]
    }

    override fun unset(index: Int) {
        wrapped.data.storage[index] = 0
    }

    override fun clone(): PalettedStorage<T> {
        return WrappedPalettedContainer(wrapped.copy())
    }

    override fun indexIterator(): Iterator<Int> {
        return (0 until wrapped.data.storage.size).iterator()
    }

    override fun contains(type: T): Boolean {
        return wrapped.data.palette.maybeHas { it == type }
    }

    override fun set(index: Int, type: T) {
        val id = wrapped.data.palette.idFor(type)
        wrapped.data.storage[index] = id
    }

    override fun count(type: T): Int {
        val id = wrapped.data.palette.idFor(type)
        return (0 until wrapped.data.storage.size)
            .count { wrapped.data.storage[it] == id }
    }

    override fun iterator(): Iterator<Indexed<T>> {
        return indexIterator().asSequence()
            .map { Indexed(it, get(it)) }
            .iterator()
    }

}