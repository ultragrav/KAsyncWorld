package net.ultragrav.kasyncworld.world.schematic.impl

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.shape.ShapedRegion
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.block.position.PositionedBlock
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.section.AsyncChunkSection
import net.ultragrav.kasyncworld.world.chunk.getSectionIndexMB
import net.ultragrav.kasyncworld.world.schematic.Dimensions
import net.ultragrav.kasyncworld.world.schematic.Schematic
import org.bukkit.Material
import org.bukkit.block.data.BlockData

internal class SchematicImplV0(override val dimensions: Dimensions) : Schematic {

    private val heightOptions: ChunkHeightOptions
    val chunks = mutableMapOf<ChunkPos, AsyncChunk>()

    init {
        val sectionCount = (dimensions.y shr 4) + 1
        heightOptions = ChunkHeightOptions(sectionCount, 0)
    }

    private fun chunkAt(x: Int, z: Int): AsyncChunk {
        return chunks.getOrPut(ChunkPos(x, z)) {
            AW.editingChunkFactory.createChunk(heightOptions)
        }
    }

    private fun checkBounds(x: Int, y: Int, z: Int) {
        require(x in 0..<dimensions.x) { "x out of bounds: $x" }
        require(y in 0..<dimensions.y) { "y out of bounds: $y" }
        require(z in 0..<dimensions.z) { "z out of bounds: $z" }
    }

    private fun checkBoundsBool(x: Int, y: Int, z: Int): Boolean {
        return x in 0..<dimensions.x && y in 0..<dimensions.y && z in 0..<dimensions.z
    }

    private fun chunkAtNoCreate(x: Int, z: Int): AsyncChunk? {
        return chunks[ChunkPos(x, z)]
    }

    override fun iterator(): Iterator<PositionedBlock> {
        return chunks.entries.asSequence()
            .flatMap { (pos, chunk) ->
                val bx = pos.x shl 4
                val bz = pos.z shl 4
                chunk.sections.asSequence()
                    .flatMapIndexed { index, section ->
                        if (section == null) return@flatMapIndexed emptySequence<PositionedBlock>()
                        // sectionY should always be same as index?
                        // just in case:
                        val sectionY = chunk.heightOptions.getSectionIndexMB(index)
                        val sectionBaseY = sectionY shl 4
                        section.blocks.iterator().asSequence()
                            .map { (pos, block) ->
                                val x = section.getBlockX(pos) + bx
                                val y = section.getBlockY(pos) + sectionBaseY
                                val z = section.getBlockZ(pos) + bz
                                PositionedBlock(x, y, z, block)
                            }
                    }
            }.filter { checkBoundsBool(it.x, it.y, it.z) }
            .iterator()
    }

    override fun setBlock(x: Int, y: Int, z: Int, block: BlockState) {
        checkBounds(x, y, z)
        val chunk = chunkAt(x shr 4, z shr 4)
        chunk.setBlock(x and 15, y, z and 15, block)
    }

    override fun setBlockData(x: Int, y: Int, z: Int, block: BlockData) {
        checkBounds(x, y, z)
        val chunk = chunkAt(x shr 4, z shr 4)
        chunk.setBlockData(x and 15, y, z and 15, block)
    }

    override fun getBlock(x: Int, y: Int, z: Int): BlockState {
        checkBounds(x, y, z)
        val chunk = chunkAtNoCreate(x shr 4, z shr 4) ?: return Blocks.AIR.defaultBlockState()
        return chunk.getBlock(x and 15, y, z and 15)
    }

    override fun getBlockData(x: Int, y: Int, z: Int): BlockData {
        checkBounds(x, y, z)
        val chunk = chunkAtNoCreate(x shr 4, z shr 4) ?: return Material.AIR.createBlockData()
        return chunk.getBlockData(x and 15, y, z and 15)
    }

    override fun unsetBlock(x: Int, y: Int, z: Int) {
        checkBounds(x, y, z)
        val chunk = chunkAtNoCreate(x shr 4, z shr 4) ?: return
        chunk.unsetBlock(x and 15, y, z and 15)
    }

    override fun setBlockEntity(x: Int, y: Int, z: Int, tag: CompoundTag) {
        checkBounds(x, y, z)
        val chunk = chunkAt(x shr 4, z shr 4)
        chunk.setBlockEntity(x and 15, y, z and 15, tag)
    }

    override fun getBlockEntity(x: Int, y: Int, z: Int): CompoundTag? {
        checkBounds(x, y, z)
        val chunk = chunkAtNoCreate(x shr 4, z shr 4) ?: return null
        return chunk.getBlockEntity(x and 15, y, z and 15)
    }

    override fun removeBlockEntity(x: Int, y: Int, z: Int) {
        checkBounds(x, y, z)
        val chunk = chunkAtNoCreate(x shr 4, z shr 4) ?: return
        chunk.removeBlockEntity(x and 15, y, z and 15)
    }

    override fun blockEntities(): Map<AWBlockPosition, CompoundTag> {
        return chunks.flatMap { (chunkPos, chunk) ->
            val cp = AWBlockPosition(chunkPos.x shl 4, 0, chunkPos.z shl 4)
            chunk.blockEntities.entries.map {
                (it.key + cp) to it.value
            }
        }.toMap()
    }

    override fun entities(): List<CompoundTag> {
        return chunks.values.flatMap { it.entities }
    }

    override fun types(): Set<BlockState> {
        return chunks.values.flatMap { it.types() }.toSet()
    }

    override fun typesData(): Set<BlockData> {
        return chunks.values.flatMap { it.typesData() }.toSet()
    }

    override fun clone(): Schematic {
        val schematic = SchematicImplV0(dimensions)
        val chunks = chunks.map { (pos, chunk) ->
            pos to chunk.clone()
        }.toMap()
        schematic.chunks.putAll(chunks)
        return schematic
    }
}