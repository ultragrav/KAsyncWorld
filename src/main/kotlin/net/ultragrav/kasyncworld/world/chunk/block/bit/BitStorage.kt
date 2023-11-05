package net.ultragrav.kasyncworld.world.chunk.block.bit

class BitStorage(
    override val size: Int,
    override val bits: Int
) : NumberStorage {

    init {
        require(bits in 1..64) { "bits must be in range 1..64" }
    }

    private val mask = (1L shl bits) - 1L
    private val elementsPerLong = 64 / bits
    private val data = LongArray((size + elementsPerLong - 1) / elementsPerLong)

    override fun get(index: Int): Int {
        val longIndex = index / elementsPerLong
        val bitIndex = index % elementsPerLong
        val shift = bitIndex * bits
        return ((data[longIndex] ushr shift) and mask).toInt()
    }

    override fun set(index: Int, value: Int) {
        val longIndex = index / elementsPerLong
        val bitIndex = index % elementsPerLong
        val shift = bitIndex * bits
        data[longIndex] = data[longIndex] and (mask shl shift).inv() or
                (value.toLong() and mask shl shift)
    }

    override fun raw(): LongArray {
        return data
    }

    override fun clone(): NumberStorage {
        val storage = BitStorage(size, bits)
        System.arraycopy(data, 0, storage.data, 0, data.size)
        return storage
    }
}