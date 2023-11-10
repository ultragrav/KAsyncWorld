package net.ultragrav.kasyncworld.world.chunk.block.storage

import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage

class PalettedStorageImpl<T>(
    private val config: PalettedStorageImplConfig<T>,
    initialBits: Int = 4
) : PalettedStorage<T> {

    override val size = config.size

    var storage = config.createStorage(initialBits)
        private set
    var palette = config.createPalette()
        private set
    override var iterationStrategy = config.createIterationStrategy()
        private set

    override val fastCountsAndTypesSupported = true

    private var counts = config.createCounter(initialBits)

    init {
        val defaultId = palette.getId(config.defaultState)
        counts.set(defaultId, config.size)
        if (defaultId != 0) {
            (0 until config.size).forEach { storage.set(it, defaultId) }
        }
    }

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
            resize()
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

    private fun resize(newBits: Int = storage.bits + 1, copy: Boolean = true) {
        val newStorage = config.createStorage(newBits)
        if (copy) {
            for (i in 0 until storage.size) {
                newStorage.set(i, storage.get(i))
            }
        }
        storage = newStorage

        val newCounts = config.createCounter(newBits)
        if (copy) {
            for (i in counts.types()) {
                newCounts.set(i, counts.get(i))
            }
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

    override fun write(output: DataWriter) {

        // Write the number of bits we're using per entry
        output.writeInt(storage.bits)

        // Write the number of entries
        output.writeInt(storage.size)

        // Write the palette according to global palette
        val globalPalette = palette.globalPalette()
        val ids = palette.listIds()
        output.writeInt(ids.size)
        ids.forEach { localId ->
            val globalId = globalPalette.getId(palette.getState(localId))
            output.writeInt(localId)
            output.writeInt(globalId)

            // We will write counts here too because it's convenient
            output.writeInt(counts.get(localId))
        }

        // Write the storage
        val longs = storage.raw()
        output.writeLongArray(longs)
    }

    override fun read(input: DataReader) {
        val bits = input.readInt()
        val size = input.readInt()
        require(size == config.size) { "Size mismatch, expected ${config.size} but got $size" }
        resize(bits, false)

        // Clear palette
        palette = config.createPalette()
        palette.getId(config.defaultState)

        val globalPalette = palette.globalPalette()

        val encodedIdToLocalId = mutableMapOf<Int, Int>()

        // Read the palette
        val paletteSize = input.readInt()
        repeat(paletteSize) {
            val encodedId = input.readInt()
            val globalId = input.readInt()
            val count = input.readInt()
            val localId = palette.getId(globalPalette.getState(globalId))
            encodedIdToLocalId[encodedId] = localId
            counts.set(localId, count)
        }

        // Read the storage, this will be a bit different from the writing
        // since we need to convert to our own local palette's ids
        val longs = input.readLongArray()
        val bitStorage = BitStorage(size, bits)
        bitStorage.useRaw(longs)
        for (index in 0 until size) {
            val encodedId = bitStorage.get(index)
            val localId = encodedIdToLocalId[encodedId] ?: error("Unknown encoded id $encodedId")
            storage.set(index, localId)
        }
    }
}