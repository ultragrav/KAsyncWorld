package net.ultragrav.kasyncworld.world.chunk.block.iteration

class NormalIteration(override val size: Int) : IterationStrategy {

    override val count = size

    override fun set(index: Int) {}
    override fun unset(index: Int) {}

    override fun setAll() {}
    override fun unsetAll() {}

    override fun contains(index: Int) = true

    override fun iterator(): Iterator<Int> {
        return (0..<size).iterator()
    }

    override fun clone() = NormalIteration(size)

}