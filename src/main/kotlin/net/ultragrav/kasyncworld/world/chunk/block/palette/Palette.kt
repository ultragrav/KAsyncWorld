package net.ultragrav.kasyncworld.world.chunk.block.palette

import org.bukkit.block.data.BlockData

interface Palette<T> {
    val size: Int
    fun getId(subject: T): Int
    fun getState(id: Int): T
    fun isMapped(subject: T): Boolean
    fun isMapped(id: Int): Boolean
    fun globalPalette(): Palette<T>
    fun localToGlobal(): Map<Int, Int>
    fun clone(): Palette<T>
}