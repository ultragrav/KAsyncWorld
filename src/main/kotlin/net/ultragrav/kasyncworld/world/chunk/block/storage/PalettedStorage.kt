package net.ultragrav.kasyncworld.world.chunk.block.storage

import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette

interface PalettedStorage<T> : Iterable<Indexed<T>> {
    val storage: NumberStorage
    val palette: Palette<T>
    val iterationStrategy: IterationStrategy
    val fastCountsAndTypesSupported: Boolean
    fun count(type: T): Int
    fun types(): Set<T>
    fun get(index: Int): T
    fun set(index: Int, type: T)
    fun unset(index: Int)
    fun clone(): PalettedStorage<T>
    fun indexIterator(): Iterator<Int>
    operator fun contains(type: T): Boolean

    fun applyTo(other: PalettedStorage<T>) {
        for (i in indexIterator()) {
            other.set(i, get(i))
        }
    }
}