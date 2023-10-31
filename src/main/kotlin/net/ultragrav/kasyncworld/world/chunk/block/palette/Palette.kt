package net.ultragrav.kasyncworld.world.chunk.block.palette

interface Palette<T> {
    val size: Int
    fun getId(type: T): Int
    fun getState(id: Int): T
    fun isMapped(type: T): Boolean
    fun isMapped(id: Int): Boolean
    fun globalPalette(): Palette<T>
    fun clone(): Palette<T>
}