package net.ultragrav.kasyncworld.world.block.block

import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImpl
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImplConfig
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.IntCounts
import net.ultragrav.kasyncworld.world.chunk.block.count.TypeCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.ChangeIterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.palette.SimplePalette
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PalettedStorageTest {

    val AIR = EmptyBlockData(Material.AIR)
    val STONE = EmptyBlockData(Material.STONE)

    private fun createStorage(): PalettedStorageImpl<BlockData> {
        val config = object : PalettedStorageImplConfig<BlockData> {
            override val size = 4096
            override val defaultState = AIR

            override fun createStorage(bits: Int): NumberStorage =
                BitStorage(bits, size)

            override fun createCounter(bits: Int): TypeCounts {
                return IntCounts(bits, size)
            }

            override fun createPalette(localToGlobal: Map<Int, Int>): Palette<BlockData> {
                val palette = SimplePalette<BlockData>()
                palette.getId(defaultState)
                return palette
            }

            override fun createIterationStrategy(): IterationStrategy =
                ChangeIterationStrategy(size)

        }

        return PalettedStorageImpl(config, 1)
    }

    @Test
    fun testGetset() {
        val storage = createStorage()
        storage.set(0, STONE)
        assert(storage.get(0) == STONE)
        assert(storage.get(1) == AIR)
    }

    @Test
    fun unset() {
        val storage = createStorage()
        storage.set(0, STONE)
        storage.unset(0)
        assert(storage.get(0) == AIR)
    }

    @Test
    operator fun iterator() {
        val storage = createStorage()

        storage.set(0, STONE)
        storage.set(1, STONE)
        storage.set(2, STONE)
        storage.set(9, STONE)
        storage.set(10, STONE)
        storage.set(11, STONE)
        storage.set(20, STONE)
        storage.set(19, STONE)
        storage.set(18, STONE)
        storage.set(16, AIR)

        val expected = listOf(
            STONE, STONE, STONE,
            STONE, STONE, STONE,
            STONE, STONE, STONE,
            AIR
        )

        val actual = storage.map { it.subject }
        assertEquals(expected, actual)
    }
}