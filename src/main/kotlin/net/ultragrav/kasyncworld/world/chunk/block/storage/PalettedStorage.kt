package net.ultragrav.kasyncworld.world.chunk.block.storage

import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import java.io.DataInput
import java.io.DataOutput

interface PalettedStorage<T> : Iterable<Indexed<T>> {
    val size: Int
    val iterationStrategy: IterationStrategy
    val fastCountsAndTypesSupported: Boolean
    fun count(type: T): Int
    fun types(): Set<T>
    operator fun get(index: Int): T
    operator fun set(index: Int, type: T)
    fun unset(index: Int)
    fun clone(): PalettedStorage<T>
    fun indexIterator(): Iterator<Int>
    operator fun contains(type: T): Boolean

    fun write(output: DataWriter)
    fun read(input: DataReader)

    fun applyTo(other: PalettedStorage<T>) {
        for (i in indexIterator()) {
            other.set(i, get(i))
        }
    }
}