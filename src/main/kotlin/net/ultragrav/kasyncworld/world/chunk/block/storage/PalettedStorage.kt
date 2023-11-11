package net.ultragrav.kasyncworld.world.chunk.block.storage

import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import java.io.DataInput
import java.io.DataOutput

/**
 * A storage for values of type [T] that are mapped to integers using a [Palette].
 * Iteration over the storage is facilitated by an [IterationStrategy] which will
 * govern which indices are iterated over and in what order. Ordering of the iteration
 * strategy is not guaranteed to be preserved when written/read from a DataWriter/DataReader
 * in order to reduce the amount of data that needs to be written/read.
 * @param T The type of value to store.
 */
interface PalettedStorage<T> : Iterable<Indexed<T>> {
    val size: Int
    val iterationStrategy: IterationStrategy
    val fastCountsAndTypesSupported: Boolean
    fun count(type: T): Int
    fun types(): Set<T>
    operator fun contains(type: T): Boolean
    operator fun get(index: Int): T
    operator fun set(index: Int, type: T)
    fun unset(index: Int)
    fun clone(): PalettedStorage<T>
    fun indexIterator(): Iterator<Int>

    fun write(output: DataWriter)
    fun read(input: DataReader)

    fun applyTo(other: PalettedStorage<T>) {
        for (i in indexIterator()) {
            other[i] = this[i]
        }
    }
}