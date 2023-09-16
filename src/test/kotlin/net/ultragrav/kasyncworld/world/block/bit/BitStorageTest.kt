package net.ultragrav.kasyncworld.world.block.bit

import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class BitStorageTest {

    @Test
    fun testSizeTen() {
        val storage = BitStorage(4, 10)
        for (i in 0 until 10) { storage.set(i, i) }
        for (i in 0 until 10) { assertEquals(i, storage.get(i)) }
    }

    @Test
    fun testLarge() {
        val storage = BitStorage(12, 4096)
        for (i in 0 until 4096) { storage.set(i, i) }
        for (i in 0 until 4096) { assertEquals(i, storage.get(i)) }
    }

}