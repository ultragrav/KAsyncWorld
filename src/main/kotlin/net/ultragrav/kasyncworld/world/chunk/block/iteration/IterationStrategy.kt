package net.ultragrav.kasyncworld.world.chunk.block.iteration

/**
 * Iterates over some or all of a set of indices.
 */
interface IterationStrategy : Iterable<Int> {

    val size: Int

    /**
     * Count of indices that will be iterated over.
     */
    val count: Int

    fun set(index: Int)
    fun unset(index: Int)

    fun setAll()
    fun unsetAll()

    operator fun contains(index: Int): Boolean

    fun clone(): IterationStrategy

}