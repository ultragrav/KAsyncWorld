package net.ultragrav.kasyncworld.world.chunk.block.count

interface TypeCounts {
    fun get(type: Int): Int
    fun set(type: Int, count: Int)
    fun add(type: Int, count: Int)
    fun increment(type: Int) = add(type, 1)
    fun decrement(type: Int) = add(type, -1)
    fun types(): Set<Int>
}