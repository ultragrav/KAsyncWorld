package net.ultragrav.kasyncworld.world.chunk.block.storage

import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage

class PalettedStorageImpl<T>(
    val config: PalettedStorageImplConfig<T>,
    initialBits: Int = 4
) : PalettedStorage<T> {

    override val size = config.size

    var storage = config.createStorage(initialBits)
    var palette = config.createPalette()
    override var iterationStrategy = config.createIterationStrategy()

    override val fastCountsAndTypesSupported = true

    private var counts = config.createCounter(initialBits)

    private val defaultId get() = palette.getId(config.defaultState)

    init {
        counts.set(defaultId, iterationStrategy.count)
        if (defaultId != 0) {
            (0..<config.size).forEach { storage.set(it, defaultId) }
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
        if (index in iterationStrategy) {
            counts.decrement(existing)
        }

        storage.set(index, num)
        counts.increment(num)
        iterationStrategy.set(index)
    }

    override fun unset(index: Int) {
        this[index] = config.defaultState
        iterationStrategy.unset(index)
        counts.decrement(defaultId)
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
            for (i in 0..<storage.size) {
                newStorage.set(i, storage.get(i))
            }
        }
        storage = newStorage

        val newCounts = config.createCounter(newBits)
        if (copy) {
            for (i in counts.types()) {
                newCounts.set(i, counts.get(i))
            }
        } else {
            newCounts.set(defaultId, iterationStrategy.count)
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
        ids.sorted().forEach { localId ->
            val globalId = globalPalette.getId(palette.getState(localId))
            output.writeInt(localId)
            output.writeInt(globalId)

            // We will write counts here too because it's convenient
            output.writeInt(counts.get(localId))
        }

        // Write the storage
        val longs = storage.raw()
        output.writeLongArray(longs)

        // Iteration data (Order is not preserved)
        val allFilled = iterationStrategy.count == iterationStrategy.size
        output.writeBoolean(allFilled)
        if (!allFilled) {
            val indexingBitStorage = BitStorage(storage.size, 1)
            indexIterator().forEach { indexingBitStorage.set(it, 1) }
            val indexingLongs = indexingBitStorage.raw()
            output.writeLongArray(indexingLongs)
        }
    }

    override fun read(input: DataReader) {
        val bits = input.readInt()
        val size = input.readInt()
        require(size == config.size) { "Size mismatch, expected ${config.size} but got $size" }
        resize(bits, false)

        // Clear palette
        palette = config.createPalette()
        // We don't need to worry about the default state
        // there is the possibility this read storage will
        // not contain an entry for the default state, but
        // this means the default state exists nowhere in the
        // storage and if unset is called on it, it will be
        // added to the palette anyway.

        // New counts
        counts = config.createCounter(bits)

        val globalPalette = palette.globalPalette()

        val encodedIdToLocalId = mutableMapOf<Int, Int>()

        // Read the palette
        val paletteSize = input.readInt()
        if (paletteSize > 1 shl bits) {
            error("Palette size $paletteSize is too big for $bits bits")
        }

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
        for (index in 0..<size) {
            val encodedId = bitStorage.get(index)
            val localId = encodedIdToLocalId[encodedId] ?: error("Unknown encoded id $encodedId")
            storage.set(index, localId)
        }

        // Read iteration data
        val allFilled = input.readBoolean()
        if (allFilled) {
            iterationStrategy = config.createIterationStrategy()
            iterationStrategy.setAll()
        } else {
            val indexingLongs = input.readLongArray()
            val indexingBitStorage = BitStorage(size, 1)
            indexingBitStorage.useRaw(indexingLongs)
            iterationStrategy = config.createIterationStrategy()
            for (index in 0..<size) {
                if (indexingBitStorage.get(index) == 1) {
                    iterationStrategy.set(index)
                }
            }
        }
    }

    fun recount() {
        counts = config.createCounter(storage.bits)
        for (i in iterationStrategy.iterator()) {
            counts.increment(storage.get(i))
        }
    }

    override fun hash(): Int {
        var result = palette.hash()
        result = 31 * result + storage.hash()
        result = 31 * result + counts.hash()
        result = 31 * result + iterationStrategy.hash()
        return result
    }
}