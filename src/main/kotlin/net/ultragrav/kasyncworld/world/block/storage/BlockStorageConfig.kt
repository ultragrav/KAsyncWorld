package net.ultragrav.kasyncworld.world.block.storage

import net.ultragrav.kasyncworld.world.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.block.palette.BlockPalette
import org.bukkit.block.data.BlockData

interface BlockStorageConfig {
    val size: Int
    val defaultState: BlockData
    fun createStorage(bits: Int): NumberStorage
    fun createPalette(localToGlobal: Map<Int, Int> = emptyMap()): BlockPalette
    fun createIterationStrategy(): IterationStrategy
}