package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage

internal class WrappedBitStorage(val storage: net.minecraft.util.BitStorage) : NumberStorage {
    override val size: Int
        get() = storage.size
    override val bits: Int
        get() = storage.bits

    override fun get(index: Int): Int {
        return storage[index]
    }

    override fun set(index: Int, value: Int) {
        storage[index] = value
    }

    override fun raw(): LongArray {
        return storage.raw
    }

    override fun clone(): NumberStorage {
        return WrappedBitStorage(storage.copy())
    }
}