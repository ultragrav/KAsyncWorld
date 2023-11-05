package net.ultragrav.kasyncworld.world.chunk.block.iteration

import net.ultragrav.kasyncworld.ceilLog2
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage

class LinkedChangeIteration(override val size: Int) : IterationStrategy {

    private var numChanges = 0
    private var current = 0

    // Index 0 is where the initial index is stored. All other indexes are shifted
    // by +1.
    private val forwards = BitStorage(size + 1, ceilLog2(size + 1))

    // Index 0 unused.
    private val backwards = BitStorage(size + 1, ceilLog2(size + 1))

    override fun set(index: Int) {
        val realIndex = index + 1
        val old = forwards.get(realIndex)
        if (old != 0) return

        forwards.set(current, realIndex)
        backwards.set(realIndex, current)
        current = realIndex
        numChanges++
    }

    override fun unset(index: Int) {
        val realIndex = index + 1

        if (!contains(index)) return

        val prev = backwards.get(realIndex)
        val next = forwards.get(realIndex)

        if (next != 0) {
            backwards.set(next, prev)
        } else {
            current = prev
        }

        forwards.set(prev, next)

        backwards.set(realIndex, 0)
        forwards.set(realIndex, 0)

        numChanges--
    }

    override fun contains(index: Int): Boolean {
        return forwards.get(index + 1) != 0 || index == current - 1
    }

    override fun clone(): IterationStrategy {
        val strategy = LinkedChangeIteration(size)
        strategy.numChanges = numChanges
        strategy.current = current
        System.arraycopy(forwards.raw(), 0, strategy.forwards.raw(), 0, forwards.raw().size)
        System.arraycopy(backwards.raw(), 0, strategy.backwards.raw(), 0, backwards.raw().size)
        return strategy
    }

    override fun iterator(): Iterator<Int> {

        val order = IntArray(numChanges)
        var index = 0

        var curr = forwards.get(0)
        while (curr != 0) {
            order[index++] = curr - 1
            curr = forwards.get(curr)
        }

        return object : Iterator<Int> {
            var i = 0
            override fun hasNext(): Boolean {
                return i < order.size
            }

            override fun next(): Int {
                return order[i++]
            }
        }
    }
}