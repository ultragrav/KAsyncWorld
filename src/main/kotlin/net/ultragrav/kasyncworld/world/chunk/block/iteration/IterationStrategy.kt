package net.ultragrav.kasyncworld.world.chunk.block.iteration

/**
 * Iterates over indices in some order.
 */
interface IterationStrategy : Iterable<Int> {

    val size: Int

    fun set(index: Int)
    fun unset(index: Int)
    operator fun contains(index: Int): Boolean

    fun clone(): IterationStrategy

}