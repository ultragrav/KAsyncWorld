package net.ultragrav.kasyncworld.world.chunk.block.iteration

import net.ultragrav.kasyncworld.ceilLog2
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage

class ChangeIterationStrategy(override  val size: Int) : IterationStrategy {

    private var numChanges = 0
    private var current = 0
    private val forwards = BitStorage(ceilLog2(size + 1), size + 1)
    private val backwards = BitStorage(ceilLog2(size + 1), size + 1)

    override fun set(index: Int) {
        val realIndex = index + 1
        val old = forwards.get(realIndex)
        if (old == 0) {
            forwards.set(current, realIndex)
            backwards.set(realIndex, current)
            current = realIndex
            numChanges++
        }
    }

    override fun unset(index: Int) {
        val realIndex = index + 1
        val prev = backwards.get(realIndex)
        val prevNext = forwards.get(prev)
        val next = forwards.get(realIndex)

        // Is it even in the chain?
        if (prevNext != realIndex) return

        if (next != 0) {
            backwards.set(next, prev)
        }

        forwards.set(prev, next)

        backwards.set(realIndex, 0)
        forwards.set(realIndex, 0)

        numChanges--
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