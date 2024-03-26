package net.ultragrav.kasyncworld.world.chunk.contract.section

import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorage

interface AsyncChunkSection {
    val blocks: PalettedStorage<BlockState>
    val biomes: PalettedStorage<Holder<Biome>>

    fun getBlockIndex(x: Int, y: Int, z: Int): Int {
        return y and 15 shl 8 or (z and 15 shl 4) or (x and 15)
    }

    fun getBlockX(index: Int) = Companion.getBlockX(index)

    fun getBlockY(index: Int) = Companion.getBlockY(index)

    fun getBlockZ(index: Int) = Companion.getBlockZ(index)

    fun setBlock(x: Int, y: Int, z: Int, block: BlockState) {
        blocks[getBlockIndex(x, y, z)] = block
    }

    fun getBlock(x: Int, y: Int, z: Int): BlockState {
        return blocks.get(getBlockIndex(x, y, z))
    }

    fun unsetBlock(x: Int, y: Int, z: Int) {
        blocks.unset(getBlockIndex(x, y, z))
    }

    fun getBiomeIndex(x: Int, y: Int, z: Int): Int {
        return y and 3 shl 4 or (z and 3 shl 2) or (x and 3)
    }

    fun getBiomeX(index: Int): Int {
        return index and 3
    }

    fun getBiomeY(index: Int): Int {
        return index shr 4 and 3
    }

    fun getBiomeZ(index: Int): Int {
        return index shr 2 and 3
    }

    fun getBiome(x: Int, y: Int, z: Int): Holder<Biome> {
        return biomes.get(getBiomeIndex(x, y, z))
    }

    fun setBiome(x: Int, y: Int, z: Int, biome: Holder<Biome>) {
        biomes.set(getBiomeIndex(x, y, z), biome)
    }

    fun clone(): AsyncChunkSection

    companion object {
        fun getBlockX(index: Int): Int {
            return index and 15
        }
        fun getBlockY(index: Int): Int {
            return index shr 8 and 15
        }
        fun getBlockZ(index: Int): Int {
            return index shr 4 and 15
        }
    }
}