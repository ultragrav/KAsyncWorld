package net.ultragrav.kasyncworld.world.block.block.iteration

import net.ultragrav.kasyncworld.world.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.block.iteration.ChangeIterationStrategy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChangeIterationStrategyTest {
    @Test
    fun testIterationOrder() {
        val strategy = ChangeIterationStrategy(10)
        strategy.set(0)
        strategy.set(5)
        strategy.set(3)
        strategy.set(9)
        strategy.set(0)

        val storage = BitStorage(4, 10)
        storage.set(0, 1)
        storage.set(5, 2)
        storage.set(3, 3)
        storage.set(9, 4)

        val iterator = strategy.iterator()
        val results = iterator.asSequence().toList()
            .map { storage.get(it) }
        assertEquals(listOf(1, 2, 3, 4), results)
    }

    @Test
    fun testUnset() {
        val strategy = ChangeIterationStrategy(10)
        strategy.set(0)
        strategy.set(5)
        strategy.set(3)
        strategy.set(9)
        strategy.set(0)
        strategy.unset(5)
        strategy.unset(3)

        val storage = BitStorage(4, 10)
        storage.set(0, 1)
        storage.set(5, 2)
        storage.set(3, 3)
        storage.set(9, 4)

        val iterator = strategy.iterator()
        val results = iterator.asSequence().toList()
            .map { storage.get(it) }
        assertEquals(listOf(1, 4), results)
    }

    @Test
    fun testUnsetFirst() {
        val strategy = ChangeIterationStrategy(10)
        strategy.set(0)
        strategy.set(5)
        strategy.set(3)
        strategy.set(9)
        strategy.set(0)
        strategy.unset(0)

        val storage = BitStorage(4, 10)
        storage.set(0, 1)
        storage.set(5, 2)
        storage.set(3, 3)
        storage.set(9, 4)

        val iterator = strategy.iterator()
        val results = iterator.asSequence().toList()
            .map { storage.get(it) }
        assertEquals(listOf(2, 3, 4), results)
    }

    @Test
    fun testUnsetAll() {
        val strategy = ChangeIterationStrategy(10)
        strategy.set(0)
        strategy.set(5)
        strategy.set(3)
        strategy.set(9)
        strategy.set(0)
        strategy.unset(0)
        strategy.unset(5)
        strategy.unset(3)
        strategy.unset(9)


        val storage = BitStorage(4, 10)
        storage.set(0, 1)
        storage.set(5, 2)
        storage.set(3, 3)
        storage.set(9, 4)

        val iterator = strategy.iterator()
        val results = iterator.asSequence().toList()
            .map { storage.get(it) }
        assertEquals(emptyList<Int>(), results)
    }
}