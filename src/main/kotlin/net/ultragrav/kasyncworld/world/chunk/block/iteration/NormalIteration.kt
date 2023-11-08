package net.ultragrav.kasyncworld.world.chunk.block.iteration

class NormalIteration(override val size: Int) : IterationStrategy {

    override fun set(index: Int) {}
    override fun unset(index: Int) {}

    override fun setAll() {}
    override fun unsetAll() {}

    override fun contains(index: Int) = true

    override fun iterator(): Iterator<Int> {
        return (0 until size).iterator()
    }

    override fun clone() = NormalIteration(size)

}