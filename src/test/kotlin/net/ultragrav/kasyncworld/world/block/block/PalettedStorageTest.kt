package net.ultragrav.kasyncworld.world.block.block

import net.ultragrav.kasyncworld.data.GravSerializerRead
import net.ultragrav.kasyncworld.data.GravSerializerWrite
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImpl
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImplConfig
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.IntCounts
import net.ultragrav.kasyncworld.world.chunk.block.count.TypeCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.LinkedChangeIteration
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.palette.SimplePalette
import net.ultragrav.serializer.GravSerializer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PalettedStorageTest {

    val AIR = Any()
    val STONE = Any()

    private fun createStorage(): PalettedStorageImpl<Any> {
        val config = object : PalettedStorageImplConfig<Any> {
            override val size = 4096
            override val defaultState = AIR

            override fun createStorage(bits: Int): NumberStorage =
                BitStorage(size, bits)

            override fun createCounter(bits: Int): TypeCounts {
                return IntCounts(bits, size)
            }

            override fun createPalette(): Palette<Any> {
                val palette = SimplePalette<Any>()
                palette.getId(defaultState)
                palette.getId(STONE)
                return palette
            }

            override fun createIterationStrategy(): IterationStrategy =
                LinkedChangeIteration(size)

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

        storage[0] = STONE
        storage[1] = STONE
        storage[2] = STONE
        storage[9] = STONE
        storage[10] = STONE
        storage[11] = STONE
        storage[20] = STONE
        storage[19] = STONE
        storage[18] = STONE
        storage[16] = AIR

        val expected = listOf(
            STONE, STONE, STONE,
            STONE, STONE, STONE,
            STONE, STONE, STONE,
            AIR
        )

        val actual = storage.map { it.subject }
        assertEquals(expected, actual)
    }

    @Test
    fun testReadWrite() {
        val storage = createStorage()

        for (i in 0 until storage.size) {
            if (i == 10) continue
            storage[i] = if (i % 2 == 0) STONE else AIR
        }

        val writer = GravSerializerWrite(GravSerializer())
        storage.write(writer)

        val reader = GravSerializerRead(GravSerializer(writer.toByteArray(), false))
        val storage2 = createStorage()
        storage2.read(reader)

        for (i in 0 until storage.size) {
            assertEquals(storage[i], storage2[i])
        }

        val seen = BooleanArray(storage2.size)
        storage2.indexIterator().forEach {
            seen[it] = true
        }

        for (i in 0 until storage.size) {
            assertEquals(seen[i], i in storage.iterationStrategy)
        }
    }

    @Test
    fun testCounts() {
        val storage = createStorage()
        assertEquals(0, storage.count(AIR))
        assertEquals(0, storage.count(STONE))
        storage[0] = STONE
        assertEquals(1, storage.count(STONE))
        assertEquals(0, storage.count(AIR))
    }

}