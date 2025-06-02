package net.ultragrav.kasyncworld.world.chunk.block.bit

interface NumberStorage {

    val size: Int
    val bits: Int

    fun get(index: Int): Int
    fun set(index: Int, value: Int)

    fun useRaw(raw: LongArray)
    fun raw(): LongArray

    fun toIntArray(): IntArray {
        val array = IntArray(size)
        for (i in 0 until size) {
            array[i] = get(i)
        }
        return array
    }

    fun isTooBig(num: Int): Boolean {
        return num >= (1 shl bits)
    }

    fun clone(): NumberStorage
    fun hash(): Int
}