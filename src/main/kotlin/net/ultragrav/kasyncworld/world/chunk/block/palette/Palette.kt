package net.ultragrav.kasyncworld.world.chunk.block.palette

/**
 * A palette is a mapping between a type and an integer id.
 * Ids must start at 0 and must increment by 1 for each time
 * [getId] is invoked with a type that has not been mapped yet.
 */
interface Palette<T> {
    val size: Int
    fun getId(type: T): Int
    fun getState(id: Int): T
    fun isMapped(type: T): Boolean
    fun isMapped(id: Int): Boolean
    fun listIds(): Set<Int>
    fun globalPalette(): Palette<T>
    fun clone(): Palette<T>
    fun hash(): Int
}