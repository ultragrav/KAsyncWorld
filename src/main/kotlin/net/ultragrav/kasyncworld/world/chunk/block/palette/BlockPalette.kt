package net.ultragrav.kasyncworld.world.chunk.block.palette

import org.bukkit.block.data.BlockData

interface BlockPalette {
    val size: Int
    fun getId(block: BlockData): Int
    fun getState(id: Int): BlockData
    fun isMapped(block: BlockData): Boolean
    fun isMapped(id: Int): Boolean
    fun globalPalette(): BlockPalette
    fun localToGlobal(): Map<Int, Int>
}