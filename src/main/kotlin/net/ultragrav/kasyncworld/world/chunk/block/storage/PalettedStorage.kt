package net.ultragrav.kasyncworld.world.chunk.block.storage

class PalettedStorage<T>(
    private val config: PalettedStorageConfig<T>,
    initialBits: Int = 4
) : Iterable<Indexed<T>> {

    var storage = config.createStorage(initialBits)
        private set
    var palette = config.createPalette()
        private set
    private var counts = config.createCounter(initialBits)
    private var iterationStrategy = config.createIterationStrategy()

    fun count(type: T): Int {
        if (!palette.isMapped(type)) return 0
        return counts.get(palette.getId(type))
    }

    fun types(): Set<T> {
        return counts.types().map { palette.getState(it) }.toSet()
    }

    operator fun contains(type: T): Boolean {
        return count(type) > 0
    }

    fun get(index: Int): T {
        val num = storage.get(index)
        return palette.getState(num)
    }

    fun set(index: Int, block: T) {
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

    fun unset(index: Int) {
        set(index, config.defaultState)
        iterationStrategy.unset(index)
    }

    fun clone(): PalettedStorage<T> {
        val clone = PalettedStorage(config)
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
}