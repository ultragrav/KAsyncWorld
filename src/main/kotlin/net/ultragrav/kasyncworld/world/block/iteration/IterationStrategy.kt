package net.ultragrav.kasyncworld.world.block.iteration

interface IterationStrategy : Iterable<Int> {

    val size: Int

    fun set(index: Int)
    fun unset(index: Int)

}