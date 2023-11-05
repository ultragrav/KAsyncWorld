package net.ultragrav.kasyncworld.world.chunk.block.count

import net.ultragrav.kasyncworld.ceilLog2
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage

class IntCounts(bits: Int, maxCount: Int) : TypeCounts {

    val data = BitStorage(1 shl bits, ceilLog2(maxCount))
    val types = mutableSetOf<Int>()

    override fun get(type: Int): Int {
        return data.get(type)
    }

    override fun set(type: Int, count: Int) {
        data.set(type, count)
        if (count == 0) {
            types.remove(type)
        } else if (type !in types) {
            types.add(type)
        }
    }

    override fun add(type: Int, count: Int) {
        set(type, get(type) + count)
    }

    override fun types(): Set<Int> {
        return types
    }

    override fun clone(): TypeCounts {
        val counts = IntCounts(data.bits, data.size)
        System.arraycopy(data.raw(), 0, counts.data.raw(), 0, data.raw().size)
        counts.types.addAll(types)
        return counts
    }
}