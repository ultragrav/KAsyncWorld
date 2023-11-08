package net.ultragrav.kasyncworld.world.chunk.block.iteration

import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage

class FlagChangeIteration(override val size: Int) : IterationStrategy {

    private val flags = BitStorage(size, 1)

    override fun set(index: Int) {
        flags.set(index, 1)
    }

    override fun unset(index: Int) {
        flags.set(index, 0)
    }

    override fun setAll() {
        val raw = flags.raw()
        for (i in raw.indices) {
            raw[i] = 0L.inv()
        }
    }

    override fun unsetAll() {
        val raw = flags.raw()
        for (i in raw.indices) {
            raw[i] = 0L
        }
    }

    override fun contains(index: Int): Boolean {
        return flags.get(index) == 1
    }

    override fun clone(): IterationStrategy {
        val strategy = FlagChangeIteration(size)
        System.arraycopy(flags.raw(), 0, strategy.flags.raw(), 0, flags.raw().size)
        return strategy
    }

    override fun iterator(): Iterator<Int> {
        return object : Iterator<Int> {
            private var index = -1
            private var next = -1

            init {
                findNext()
            }

            private fun findNext() {
                index++
                while (index < size) {
                    if (flags.get(index) == 1) {
                        next = index
                        return
                    }
                    index++
                }
                next = -1
            }

            override fun hasNext(): Boolean {
                return next != -1
            }

            override fun next(): Int {
                val ret = next
                findNext()
                return ret
            }
        }
    }
}