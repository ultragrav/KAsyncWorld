package net.ultragrav.kasyncworld.world.chunk.block.storage

import org.bukkit.block.data.BlockData

class BlockStorage(
    private val config: BlockStorageConfig,
    initialBits: Int = 4
) : Iterable<IndexedBlockState> {

    var storage = config.createStorage(initialBits)
        private set
    val palette = config.createPalette()
    private var counts = config.createCounter(initialBits)
    private val iterationStrategy = config.createIterationStrategy()

    fun count(type: BlockData): Int {
        return synchronized(this) {
            if (!palette.isMapped(type)) return 0
            counts.get(palette.getId(type))
        }
    }

    fun types(): Set<BlockData> {
        return synchronized(this) {
            counts.types().map { palette.getState(it) }.toSet()
        }
    }

    operator fun contains(type: BlockData): Boolean {
        return synchronized(this) {
            count(type) > 0
        }
    }

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

            val existing = storage.get(index)
            counts.decrement(existing)

            storage.set(index, num)
            counts.increment(num)

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

        val newCounts = config.createCounter(newSize)
        for (i in counts.types()) {
            newCounts.set(i, counts.get(i))
        }
        counts = newCounts
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