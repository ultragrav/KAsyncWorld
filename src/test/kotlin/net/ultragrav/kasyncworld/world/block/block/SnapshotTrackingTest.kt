package net.ultragrav.kasyncworld.world.block.block

import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.IntCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.*
import net.ultragrav.kasyncworld.world.chunk.block.palette.SimplePalette
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImpl
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImplConfig
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import org.junit.jupiter.api.Test
import kotlin.test.*

class SnapshotTrackingTest {
    private fun storage(iteration: () -> IterationStrategy) = PalettedStorageImpl(object : PalettedStorageImplConfig<String> {
        override val size = 4096
        override val defaultState = "air"
        override fun createStorage(bits: Int) = BitStorage(size, bits)
        override fun createCounter(bits: Int) = IntCounts(bits, size)
        override fun createPalette() = SimplePalette<String>()
        override fun createIterationStrategy() = iteration()
    })

    @Test fun `unmarked reads retain states and count only subsequent writes`() {
        for (iteration in listOf({ FlagChangeIteration(4096) }, { LinkedChangeIteration(4096) }, { NormalIteration(4096) })) {
            val blocks = storage(iteration)
            for (i in 0 until 4096) blocks[i] = if (i % 2 == 0) "stone" else "air"
            blocks.resetReadTracking(false)
            assertEquals(0, blocks.iterationStrategy.count)
            assertTrue(blocks.types().isEmpty())
            assertFalse(blocks.iterator().hasNext())
            for (i in 0 until 4096) assertEquals(if (i % 2 == 0) "stone" else "air", blocks[i])
            blocks[12] = "dirt"
            blocks[12] = "sand"
            assertEquals(listOf(12), blocks.indexIterator().asSequence().toList())
            assertEquals(1, blocks.count("sand"))
            assertEquals(0, blocks.count("stone"))
            assertEquals(0, blocks.count("dirt"))
            assertEquals("stone", blocks[14])
        }
    }

    @Test fun `default reads still mark all copied blocks`() {
        assertTrue(ChunkReadOptions().markBlocksChanged)
        val blocks = storage { FlagChangeIteration(4096) }
        for (i in 0 until 4096) blocks[i] = if (i % 2 == 0) "stone" else "air"
        blocks.resetReadTracking(true)
        assertEquals(4096, blocks.iterationStrategy.count)
        assertEquals(2048, blocks.count("stone"))
        assertEquals(2048, blocks.count("air"))
    }
}
