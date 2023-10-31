package net.ultragrav.kasyncworld.world.contract

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.ticks.SavedTick
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.heightmap.AWHeightMap
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSection
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSectionFactory
import org.bukkit.HeightMap
import org.bukkit.persistence.PersistentDataContainer

/**
 * A simple representation of a chunk. This representation is not
 * thread safe so access to this chunk should be managed appropriately.
 */
interface AsyncChunk : AsyncChunkSectionFactory {

    val heightOptions: ChunkHeightOptions
    val heightMaps: Map<Heightmap.Types, AWHeightMap>
    val blockEntities: Map<AWBlockPosition, CompoundTag>
    val sections: Array<AsyncChunkSection?>
    val entities: List<CompoundTag>

    var blockTicks: MutableList<SavedTick<Block>>
    var fluidTicks: MutableList<SavedTick<Fluid>>

    var persistentData: CompoundTag

    fun setBlock(x: Int, y: Int, z: Int, block: BlockState)
    fun unsetBlock(x: Int, y: Int, z: Int)
    fun getBlock(x: Int, y: Int, z: Int): BlockState

    fun getHeightMap(type: Heightmap.Types): AWHeightMap
    fun setHeightMap(type: Heightmap.Types, heightMap: AWHeightMap)
    fun clearHeightMaps()

    fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag?
    fun setBlockEntity(x: Int, y: Int, z: Int, tag: CompoundTag)
    fun removeBlockEntity(x: Int, y: Int, z: Int)
    fun clearBlockEntities()

    fun setSection(sectionIndex: Int, section: AsyncChunkSection?)
    fun getSection(sectionIndex: Int): AsyncChunkSection?

    fun clearSections()

    fun addEntity(tag: CompoundTag)
    fun removeEntity(tag: CompoundTag)
    fun clearEntities()

    fun clone(): AsyncChunk
}