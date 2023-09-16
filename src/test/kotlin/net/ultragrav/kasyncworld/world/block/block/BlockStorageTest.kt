package net.ultragrav.kasyncworld.world.block.block

import net.ultragrav.kasyncworld.world.chunk.block.storage.BlockStorage
import net.ultragrav.kasyncworld.world.chunk.block.storage.BlockStorageConfig
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.iteration.ChangeIterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.palette.BlockPalette
import net.ultragrav.kasyncworld.world.chunk.block.palette.SimpleBlockPalette
import org.bukkit.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BlockStorageTest {

    val AIR = EmptyBlockData(Material.AIR)
    val STONE = EmptyBlockData(Material.STONE)

    private fun createStorage(): BlockStorage {
        val config = object : BlockStorageConfig {
            override val size = 4096
            override val defaultState = AIR

            override fun createStorage(bits: Int): NumberStorage =
                BitStorage(bits, size)

            override fun createPalette(localToGlobal: Map<Int, Int>): BlockPalette {
                val palette = SimpleBlockPalette()
                palette.getId(defaultState)
                return palette
            }

            override fun createIterationStrategy(): IterationStrategy =
                ChangeIterationStrategy(size)

        }

        return BlockStorage(config, 1)
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