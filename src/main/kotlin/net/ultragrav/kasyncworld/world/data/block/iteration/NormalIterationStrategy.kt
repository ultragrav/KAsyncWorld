package net.ultragrav.kasyncworld.world.data.block.iteration

import net.ultragrav.kasyncworld.world.data.bit.NumberStorage
import net.ultragrav.kasyncworld.world.data.block.IterationStrategy

class NormalIterationStrategy(override val size: Int) : IterationStrategy {

    override fun set(index: Int) {}
    override fun unset(index: Int) {}

    override fun iterator(): Iterator<Int> {
        return (0 until size).iterator()
    }

}