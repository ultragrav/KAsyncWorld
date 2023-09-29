package net.ultragrav.kasyncworld.world.chunk.block.iteration

class NormalIterationStrategy(override val size: Int) : IterationStrategy {

    override fun set(index: Int) {}
    override fun unset(index: Int) {}

    override fun iterator(): Iterator<Int> {
        return (0 until size).iterator()
    }

    override fun clone() = NormalIterationStrategy(size)

}