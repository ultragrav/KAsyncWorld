package net.ultragrav.kasyncworld.world.block.bit

import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class BitStorageTest {

    @Test
    fun testSizeTen() {
        val storage = BitStorage(10, 4)
        for (i in 0 until 10) {
            storage.set(i, i)
        }
        for (i in 0 until 10) {
            assertEquals(i, storage.get(i))
        }
    }

    @Test
    fun testLarge() {
        val storage = BitStorage(4096, 12)
        for (i in 0 until 4096) {
            storage.set(i, i)
        }
        for (i in 0 until 4096) {
            assertEquals(i, storage.get(i))
        }
    }
}