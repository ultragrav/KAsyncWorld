package net.ultragrav.kasyncworld.world.chunk.block.storage

class PalettedStorage<T>(
    private val config: PalettedStorageConfig<T>,
    initialBits: Int = 4
) : Iterable<Indexed<T>> {

    var storage = config.createStorage(initialBits)
        private set
    val palette = config.createPalette()
    private var counts = config.createCounter(initialBits)
    private val iterationStrategy = config.createIterationStrategy()

    fun count(type: T): Int {
        return synchronized(this) {
            if (!palette.isMapped(type)) return 0
            counts.get(palette.getId(type))
        }
    }

    fun types(): Set<T> {
        return synchronized(this) {
            counts.types().map { palette.getState(it) }.toSet()
        }
    }

    operator fun contains(type: T): Boolean {
        return synchronized(this) {
            count(type) > 0
        }
    }

    fun get(index: Int): T {
        return synchronized(this) {
            val num = storage.get(index)
            palette.getState(num)
        }
    }

    fun set(index: Int, block: T) {
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

    fun unset(index: Int) {
        synchronized(this) {
            set(index, config.defaultState)
            iterationStrategy.unset(index)
        }
    }

    fun copyDataFrom(other: PalettedStorage<T>) {
        require(other.config.size == config.size) { "Cannot copy data from storage with different size" }
        synchronized(this) {
            val otherBits = other.storage.bits
            if (otherBits > storage.bits) upsize(otherBits)
            for (i in 0 until other.storage.size) {
                set(i, other.get(i))
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

    override fun iterator(): Iterator<Indexed<T>> {
        val numIterator = synchronized(this) { iterationStrategy.iterator() }

        return object : Iterator<Indexed<T>> {
            override fun hasNext(): Boolean {
                return numIterator.hasNext()
            }

            override fun next(): Indexed<T> {
                val index = numIterator.next()
                val data = synchronized(this@PalettedStorage) { storage.get(index) }
                return Indexed(index, palette.getState(data))
            }
        }
    }
}