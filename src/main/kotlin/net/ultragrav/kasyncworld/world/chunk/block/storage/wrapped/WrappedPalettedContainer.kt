package net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped

import net.minecraft.core.IdMap
import net.minecraft.core.IdMapper
import net.minecraft.util.SimpleBitStorage
import net.minecraft.world.level.chunk.PalettedContainer
import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.bit.NumberStorage
import net.ultragrav.kasyncworld.world.chunk.block.count.TypeCounts
import net.ultragrav.kasyncworld.world.chunk.block.iteration.IterationStrategy
import net.ultragrav.kasyncworld.world.chunk.block.iteration.NormalIteration
import net.ultragrav.kasyncworld.world.chunk.block.palette.Palette
import net.ultragrav.kasyncworld.world.chunk.block.storage.Indexed
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage
import org.bukkit.block.BlockState

// This class did not turn out to be doing what I originally intended :/

class WrappedPalettedContainer<T : Any>(
    override val wrapped: PalettedContainer<T>,
    override val iterationStrategy: IterationStrategy =
        NormalIteration(wrapped.data.storage.size)
) : MinecraftPalettedStorage<T> {

    override val fastCountsAndTypesSupported = false

    override val size: Int
        get() = wrapped.size()

    override fun setRaw(storage: NumberStorage, palette: Palette<T>, counts: TypeCounts) {
        require(storage.size == size) { "Storage sizes must match" }

        var conf = wrapped.data.configuration
        val constructor = conf::class.java.declaredConstructors.first()
        if (!constructor.trySetAccessible()) error("Failed to access constructor of ${conf::class.java.name}")
        val newConf = constructor.newInstance(PalettedContainer.Strategy.LINEAR_PALETTE_FACTORY, storage.bits)

        val method = net.minecraft.world.level.chunk.Palette.Factory::class.java.methods.first()
        val lst = (0..<palette.size).map { palette.getState(it) }
        val newPalette = method.invoke(PalettedContainer.Strategy.LINEAR_PALETTE_FACTORY, storage.bits, wrapped.registry, wrapped, lst) as net.minecraft.world.level.chunk.Palette<T>
        val newStorage = SimpleBitStorage(storage.bits, storage.size, storage.raw().copyOf())
        val newData = PalettedContainer.Data::class.java.constructors.first().newInstance(newConf, newStorage, newPalette)
        wrapped.data = newData as PalettedContainer.Data<T>
    }

    override fun types(): Set<T> {
        return (0..<wrapped.data.palette.size)
            .map { wrapped.data.palette.valueFor(it) }
            .toSet()
    }

    override fun get(index: Int): T {
        return wrapped[index]
    }

    override fun unset(index: Int) {
        wrapped.data.storage[index] = 0
        iterationStrategy.unset(index)
    }

    override fun clone(): PalettedStorage<T> {
        return WrappedPalettedContainer(wrapped.copy())
    }

    override fun indexIterator(): Iterator<Int> {
        return iterationStrategy.iterator()
    }

    override fun write(output: DataWriter) {
        output.writeInt(size)
        for (i in 0..<size) {
            val id = wrapped.registry.getId(get(i))
            output.writeInt(id)
        }
    }

    override fun read(input: DataReader) {
        val inputSize = input.readInt()
        if (inputSize != size) {
            throw IllegalArgumentException("Size mismatch, expected $size but got $inputSize")
        }
        for (i in 0..<size) {
            val id = input.readInt()
            set(i, wrapped.registry.byIdOrThrow(id))
        }
    }

    override fun hash(): Int {
        return wrapped.hashCode()
    }

    override fun contains(type: T): Boolean {
        return wrapped.data.palette.maybeHas { it == type }
    }

    override fun set(index: Int, type: T) {
        wrapped.set(index, type)
        iterationStrategy.set(index)
    }

    override fun count(type: T): Int {
        val id = wrapped.data.palette.idFor(type)
        return (0..<wrapped.data.storage.size)
            .count { wrapped.data.storage[it] == id }
    }

    override fun iterator(): Iterator<Indexed<T>> {
        return indexIterator().asSequence()
            .map { Indexed(it, get(it)) }
            .iterator()
    }

}