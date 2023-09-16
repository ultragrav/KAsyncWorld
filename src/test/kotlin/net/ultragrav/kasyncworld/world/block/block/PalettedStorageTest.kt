package net.ultragrav.kasyncworld.world.block.block

import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageConfig
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.IntCounts
import net.ultragrav.kasyncworld.world.chunk.block.count.TypeCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.ChangeIterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.palette.SimplePalette
import org.bukkit.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PalettedStorageTest {

    val AIR = EmptyBlockData(Material.AIR)
    val STONE = EmptyBlockData(Material.STONE)

    private fun createStorage(): PalettedStorage {
        val config = object : PalettedStorageConfig {
            override val size = 4096
            override val defaultState = AIR

            override fun createStorage(bits: Int): NumberStorage =
                BitStorage(bits, size)

            override fun createCounter(bits: Int): TypeCounts {
                return IntCounts(bits, size)
            }

            override fun createPalette(localToGlobal: Map<Int, Int>): Palette {
                val palette = SimplePalette()
                palette.getId(defaultState)
                return palette
            }

            override fun createIterationStrategy(): IterationStrategy =
                ChangeIterationStrategy(size)

        }

        return PalettedStorage(config, 1)
    }

    @Test
    fun testGetSetBlock() {
        val storage = createStorage()
        storage.setBlock(0, STONE)
        assert(storage.getBlock(0) == STONE)
        assert(storage.getBlock(1) == AIR)
    }

    @Test
    fun unsetBlock() {
        val storage = createStorage()
        storage.setBlock(0, STONE)
        storage.unsetBlock(0)
        assert(storage.getBlock(0) == AIR)
    }

    @Test
    operator fun iterator() {
        val storage = createStorage()

        storage.setBlock(0, STONE)
        storage.setBlock(1, STONE)
        storage.setBlock(2, STONE)
        storage.setBlock(9, STONE)
        storage.setBlock(10, STONE)
        storage.setBlock(11, STONE)
        storage.setBlock(20, STONE)
        storage.setBlock(19, STONE)
        storage.setBlock(18, STONE)
        storage.setBlock(16, AIR)

        val expected = listOf(
            STONE, STONE, STONE,
            STONE, STONE, STONE,
            STONE, STONE, STONE,
            AIR
        )

        val actual = storage.map { it.block }
        assertEquals(expected, actual)
    }
}