package net.ultragrav.kasyncworld.world.chunk.block.storage

class PalettedStorageImpl<T>(
    private val config: PalettedStorageImplConfig<T>,
    initialBits: Int = 4
) : PalettedStorage<T> {

    override var storage = config.createStorage(initialBits)
        private set
    override var palette = config.createPalette()
        private set
    override var iterationStrategy = config.createIterationStrategy()
        private set
    private var counts = config.createCounter(initialBits)

    override fun count(type: T): Int {
        if (!palette.isMapped(type)) return 0
        return counts.get(palette.getId(type))
    }

    override fun types(): Set<T> {
        return counts.types().map { palette.getState(it) }.toSet()
    }

    override operator fun contains(type: T): Boolean {
        return count(type) > 0
    }

    override fun get(index: Int): T {
        val num = storage.get(index)
        return palette.getState(num)
    }

    override fun set(index: Int, type: T) {
        val num = palette.getId(type)
        while (storage.isTooBig(num)) {
            upsize()
        }

        val existing = storage.get(index)
        counts.decrement(existing)

        storage.set(index, num)
        counts.increment(num)

        iterationStrategy.set(index)
    }

    override fun unset(index: Int) {
        set(index, config.defaultState)
        iterationStrategy.unset(index)
    }

    override fun clone(): PalettedStorageImpl<T> {
        val clone = PalettedStorageImpl(config)
        clone.iterationStrategy = iterationStrategy.clone()
        clone.palette = palette.clone()
        clone.storage = storage.clone()
        clone.counts = counts.clone()
        return clone
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
        return iterationStrategy.iterator()
            .asSequence()
            .map { Indexed(it, get(it)) }
            .iterator()
    }

    override fun indexIterator(): Iterator<Int> {
        return iterationStrategy.iterator()
    }
}