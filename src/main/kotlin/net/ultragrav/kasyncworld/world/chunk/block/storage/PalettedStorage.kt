package net.ultragrav.kasyncworld.world.chunk.block.storage

import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette

interface PalettedStorage<T> : Iterable<Indexed<T>> {
    val storage: NumberStorage
    val palette: Palette<T>
    fun count(type: T): Int
    fun types(): Set<T>
    fun get(index: Int): T
    fun set(index: Int, type: T)
    fun unset(index: Int)
    fun clone(): PalettedStorage<T>
    fun indexIterator(): Iterator<Int>
    operator fun contains(type: T): Boolean
}