package net.ultragrav.kasyncworld.world.data.block

import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData

class BlockStorage(
    private val config: BlockStorageConfig,
    initialBits: Int = 4
) : Iterable<IndexedBlockState> {

    private var storage = config.createStorage(initialBits)
    private var palette = config.createPalette()
    private val iterationStrategy = config.createIterationStrategy()

    fun getBlock(index: Int): BlockData {
        return synchronized(this) {
            val num = storage.get(index)
            palette.getState(num)
        }
    }

    fun setBlock(index: Int, block: BlockData) {
        synchronized(this) {
            val num = palette.getId(block)
            while (storage.isTooBig(num)) {
                upsize()
            }
            storage.set(index, num)
            iterationStrategy.set(index)
        }
    }

    fun unsetBlock(index: Int) {
        synchronized(this) {
            setBlock(index, config.defaultState)
            iterationStrategy.unset(index)
        }
    }

    fun copyDataFrom(other: BlockStorage) {
        require(other.config.size == config.size) { "Cannot copy data from storage with different size" }
        synchronized(this) {
            val otherBits = other.storage.bits
            if (otherBits > storage.bits) upsize(otherBits)
            for (i in 0 until other.storage.size) {
                setBlock(i, other.getBlock(i))
            }
        }
    }

    private fun upsize(newSize: Int = storage.bits + 1) {
        val newStorage = config.createStorage(newSize)
        for (i in 0 until storage.size) {
            newStorage.set(i, storage.get(i))
        }
        storage = newStorage
    }

    override fun iterator(): Iterator<IndexedBlockState> {
        val numIterator = synchronized(this) { iterationStrategy.iterator() }

        return object : Iterator<IndexedBlockState> {
            override fun hasNext(): Boolean {
                return numIterator.hasNext()
            }

            override fun next(): IndexedBlockState {
                val index = numIterator.next()
                val data = synchronized(this@BlockStorage) { storage.get(index) }
                return IndexedBlockState(index, palette.getState(data))
            }
        }
    }
}