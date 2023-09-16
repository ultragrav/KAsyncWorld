package net.ultragrav.kasyncworld.world.chunk.block.storage

import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.TypeCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette

interface PalettedStorageConfig<T> {
    val size: Int
    val defaultState: T
    fun createStorage(bits: Int): NumberStorage
    fun createCounter(bits: Int): TypeCounts
    fun createPalette(localToGlobal: Map<Int, Int> = emptyMap()): Palette<T>
    fun createIterationStrategy(): IterationStrategy
}