package net.ultragrav.kasyncworld.world.data.block

import net.ultragrav.kasyncworld.world.data.bit.NumberStorage

interface IterationStrategy : Iterable<Int> {

    val size: Int

    fun set(index: Int)
    fun unset(index: Int)

}