package net.ultragrav.kasyncworld.world.chunk.block.iteration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FlagChangeIterationTest {

    @Test
    fun set() {
        val iteration = FlagChangeIteration(10)
        iteration.set(3)
        assertTrue(iteration.contains(3))
    }

    @Test
    fun unset() {
        val iteration = FlagChangeIteration(10)
        iteration.set(7)
        assertTrue(iteration.contains(7))

        iteration.unset(7)
        assertFalse(iteration.contains(7))
    }

    @Test
    fun contains() {
        val iteration = FlagChangeIteration(10)
        iteration.set(5)
        iteration.set(2)
        assertTrue(iteration.contains(5))
        assertTrue(iteration.contains(2))
        assertFalse(iteration.contains(1))
        assertFalse(iteration.contains(8))
    }

    @Test
    fun iterator() {
        val iteration = FlagChangeIteration(10)
        iteration.set(0)
        iteration.set(3)
        iteration.set(6)
        iteration.set(9)

        val results = iteration.iterator().asSequence().toList()
        assertEquals(listOf(0, 3, 6, 9), results)
    }

    @Test
    fun cloneTest() {
        val original = FlagChangeIteration(10)
        original.set(2)
        original.set(5)

        val cloned = original.clone() as FlagChangeIteration

        // Check if cloned object has the same flags set
        assertTrue(cloned.contains(2))
        assertTrue(cloned.contains(5))

        // Change the state of the original and make sure the cloned one does not change
        original.unset(2)
        assertTrue(original.contains(5))
        assertFalse(original.contains(2))

        assertTrue(cloned.contains(2))
        assertTrue(cloned.contains(5))
    }
}
