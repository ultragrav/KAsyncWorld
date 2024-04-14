package net.ultragrav.kasyncworld.world.chunk.contract

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.heightmap.AsyncHeightMap
import net.ultragrav.kasyncworld.world.chunk.contract.section.AsyncChunkSection
import org.bukkit.block.Biome
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData

/**
 * Facilitates some sort of method of accessing and changing the data
 * in an [AsyncChunk] instance
 */
interface AsyncChunkAccess {

    val heightOptions: ChunkHeightOptions

    /**
     * Sets a block at the given coordinates to a given state.
     * Valid ranges for x, z are 0-15
     * Valid ranges for y are given by [heightOptions]
     */
    fun setBlock(x: Int, y: Int, z: Int, block: BlockState)

    fun setBlockData(x: Int, y: Int, z: Int, blockData: BlockData) = setBlock(x, y, z, (blockData as CraftBlockData).state)

    /**
     * Unsets a block at the given coordinates.
     * Valid ranges for x, z are 0-15
     * Valid ranges for y are given by [heightOptions]
     */
    fun unsetBlock(x: Int, y: Int, z: Int)

    fun isBlockSet(x: Int, y: Int, z: Int): Boolean

    /**
     * Gets the block at the given coordinates.
     * Valid ranges for x, z are 0-15
     * Valid ranges for y are given by [heightOptions]
     */
    fun getBlock(x: Int, y: Int, z: Int): BlockState

    fun getBlockData(x: Int, y: Int, z: Int): BlockData = getBlock(x, y, z).createCraftBlockData()

    fun types(): Set<BlockState>
    fun typesData(): Set<BlockData> = types().map { it.createCraftBlockData() }.toSet()

    operator fun contains(state: BlockState): Boolean
    operator fun contains(blockData: BlockData): Boolean = contains((blockData as CraftBlockData).state)

    /**
     * Sets the biome at the given coordinates to a given biome.
     * Valid ranges for x, z are 0-3
     * Valid ranges for y are given by [heightOptions] and is equal
     * to the height of the chunk divided by 4
     */
    fun getBiome(x: Int, y: Int, z: Int): Biome

    /**
     * Gets the biome at the given coordinates.
     * Valid ranges for x, z are 0-3
     * Valid ranges for y are given by [heightOptions] and is equal
     * to the height of the chunk divided by 4
     */
    fun setBiome(x: Int, y: Int, z: Int, biome: Biome)

    fun getHeightMap(type: Heightmap.Types): AsyncHeightMap
    fun setHeightMap(type: Heightmap.Types, heightMap: AsyncHeightMap)
    fun clearHeightMaps()

    /**
     * Get a block entity at the specified coordinates relative to this
     * chunk.
     */
    fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag?

    /**
     * Set a block entity at the specified coordinates relative to this
     * chunk.
     */
    fun setBlockEntity(x: Int, y: Int, z: Int, tag: CompoundTag)

    /**
     * Remove a block entity at the specified coordinates relative to this
     * chunk.
     */
    fun removeBlockEntity(x: Int, y: Int, z: Int)

    /**
     * Clear all block entities in this chunk.
     */
    fun clearBlockEntities()

    /**
     * Add an entity to this chunk.
     */
    fun addEntity(tag: CompoundTag)

    /**
     * Remove an entity from this chunk.
     */
    fun removeEntity(tag: CompoundTag)

    /**
     * Clear all entities in this chunk.
     */
    fun clearEntities()

    /**
     * Get a section at the specified minimum-based index. This means
     * that the valid range for this is (from [heightOptions]) [ChunkHeightOptions.minSection] to
     * [ChunkHeightOptions.minSection] + [ChunkHeightOptions.numSections] - 1
     */
    fun setSection(sectionIndexMinBased: Int, section: AsyncChunkSection?)

    /**
     * Get a section at the specified minimum-based index. This means
     * that the valid range for this is (from [heightOptions]) [ChunkHeightOptions.minSection] to
     * [ChunkHeightOptions.minSection] + [ChunkHeightOptions.numSections] - 1
     */
    fun getSection(sectionIndexMinBased: Int): AsyncChunkSection?

    fun clearSections()
}