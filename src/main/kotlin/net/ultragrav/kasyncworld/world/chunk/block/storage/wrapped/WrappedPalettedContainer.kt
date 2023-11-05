package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.PalettedContainer
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.iteration.NormalIteration
import net.ultragrav.kasyncworld.world.chunk.block.storage.Indexed
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage

class WrappedPalettedContainer<T>(
    override val wrapped: PalettedContainer<T>,
    override val iterationStrategy: IterationStrategy =
        NormalIteration(wrapped.data.storage.size)
) : MinecraftPalettedStorage<T> {

    override val fastCountsAndTypesSupported = false

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
        iterationStrategy.unset(index)
    }

    override fun clone(): PalettedStorage<T> {
        return WrappedPalettedContainer(wrapped.copy())
    }

    override fun indexIterator(): Iterator<Int> {
        return iterationStrategy.iterator()
    }

    override fun contains(type: T): Boolean {
        return wrapped.data.palette.maybeHas { it == type }
    }

    override fun set(index: Int, type: T) {
        wrapped.set(index, type)
        iterationStrategy.set(index)
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