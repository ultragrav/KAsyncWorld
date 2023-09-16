package net.ultragrav.kasyncworld.world.data.block

import net.ultragrav.kasyncworld.world.data.bit.NumberStorage
import net.ultragrav.kasyncworld.world.data.palette.BlockPalette
import org.bukkit.block.data.BlockData

interface BlockStorageConfig {
    val size: Int
    val defaultState: BlockData
    fun createStorage(bits: Int): NumberStorage
    fun createPalette(localToGlobal: Map<Int, Int> = emptyMap()): BlockPalette
    fun createIterationStrategy(): IterationStrategy
}