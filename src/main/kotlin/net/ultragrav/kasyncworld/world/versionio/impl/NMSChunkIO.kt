package net.ultragrav.kasyncworld.world.versionio.impl

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.ChunkStatus
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.LevelChunkSection
import net.minecraft.world.ticks.ProtoChunkTicks
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped.WrappedPalettedContainer
import net.ultragrav.kasyncworld.world.chunk.heightmap.AWHeightMap
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSection
import net.ultragrav.kasyncworld.world.versionio.ChunkIO
import net.ultragrav.kasyncworld.world.versionio.ChunkWriteOptions
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_20_R2.CraftChunk
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity

class NMSChunkIO : ChunkIO {

    override fun writeChunk(bukkitChunk: Chunk, chunk: AsyncChunk, options: ChunkWriteOptions) {
        val nms = (bukkitChunk as CraftChunk).getHandle(ChunkStatus.FULL)
                as? LevelChunk ?: throw IllegalStateException("Chunk is not fully loaded")

        val cx = nms.locX
        val cz = nms.locZ

        // Sections (Blocks)
        for (i in 0 until nms.sectionsCount) {
            val nmsSection = nms.sections[i] ?: continue
            val section = chunk.getSection(i) ?: continue

            // Remove tile entities at edited block positions
            section.blocks.indexIterator().forEach { index ->
                val x = section.getBlockX(index)
                val y = section.getBlockY(index)
                val z = section.getBlockZ(index)
                val pos = BlockPos(x, y, z)
                nms.removeBlockEntity(pos)
            }

            writeSection(section, nmsSection)
        }

        chunk.blockEntities.forEach { (pos, tag) ->
            val nmsPos = BlockPos(pos.x, pos.y, pos.z)
            val nmsBlockEntity = BlockEntity.loadStatic(
                nmsPos,
                chunk.getBlock(pos.x, pos.y, pos.z),
                tag
            ) ?: return@forEach
            nmsBlockEntity.level = nms.level
            nms.addAndRegisterBlockEntity(nmsBlockEntity)
        }

        // Entities
        if (!options.appendEntities) {
            nms.level.entityLookup.getOrCreateChunk(cx, cz)
                .chunkEntities
                .toList()
                .forEach {
                    it.remove()
                }
        }

        val decodedEntities = EntityType.loadEntitiesRecursive(chunk.entities, nms.level).toList()
        nms.level.entityLookup.addEntityChunkEntities(decodedEntities, ChunkPos(nms.locX, nms.locZ))

        // Persistent Data
        nms.persistentDataContainer.clear()
        nms.persistentDataContainer.putAll(chunk.persistentData)


    }

    private fun writeSection(section: AsyncChunkSection, nmsSection: LevelChunkSection) {
        // Blocks
        val wrappedStates = WrappedPalettedContainer(nmsSection.states)
        section.blocks.applyTo(wrappedStates)

        // Biomes
        val wrappedBiomes = WrappedPalettedContainer(nmsSection.biomes)
        section.biomes.applyTo(wrappedBiomes)
    }

    override fun sendPackets(bukkitChunk: Chunk, chunk: AsyncChunk) {
        TODO("Not yet implemented")
    }

    override fun readChunk(bukkitChunk: Chunk, factory: AsyncChunkFactory): AsyncChunk {
        val nms = (bukkitChunk as CraftChunk).getHandle(ChunkStatus.FULL)
                as? LevelChunk ?: throw IllegalStateException("Chunk is not fully loaded")

        val cx = nms.locX
        val cz = nms.locZ

        val height = ChunkHeightOptions(nms.sectionsCount, nms.minSection)
        val chunk = factory.createChunk(height)

        // Sections (Blocks)
        for (i in 0 until nms.sectionsCount) {
            val nmsSection = nms.sections[i] ?: continue
            val section = chunk.createSection()
            readSection(section, nmsSection)
        }

        // Block Entities
        nms.blockEntities.mapValues { it.value.saveWithFullMetadata() }
            .forEach { (pos, ent) -> chunk.setBlockEntity(pos.x, pos.y, pos.z, ent) }

        // Entities
        bukkitChunk.entities.map { it as CraftEntity }
            .map { it.handle }
            .filter { it.persist }
            .map {
                val tag = CompoundTag()
                it.save(tag)
                tag
            }
            .forEach { chunk.addEntity(it) }

        // Persistent data
        chunk.persistentData = nms.persistentDataContainer.toTagCompound()

        // Block/Fluid ticks
        val ticksForSerialization = nms.ticksForSerialization

        val blockTicksTag = ticksForSerialization.blocks.save(nms.level.gameTime) {
            BuiltInRegistries.BLOCK.getKey(it).toString()
        } as ListTag

        val fluidTicksTag = ticksForSerialization.fluids.save(nms.level.gameTime) {
            BuiltInRegistries.FLUID.getKey(it).toString()
        } as ListTag

        val blockTicks = ProtoChunkTicks.load(blockTicksTag, {
            BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(it))
        }, ChunkPos(cx, cz))

        val fluidTicks = ProtoChunkTicks.load(fluidTicksTag, {
            BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(it))
        }, ChunkPos(cx, cz))

        chunk.blockTicks = blockTicks.scheduledTicks()
        chunk.fluidTicks = fluidTicks.scheduledTicks()

        // Height Maps
        nms.heightmaps.forEach { (type, map) ->
            val newHeightMap = AWHeightMap(
                type,
                chunk
            )
            for (x in 0 until 16) {
                for (z in 0 until 16) {
                    newHeightMap.setHeight(x, z, map.getFirstAvailable(x, z))
                }
            }

            chunk.setHeightMap(type, newHeightMap)
        }

        return chunk
    }

    private fun readSection(async: AsyncChunkSection, section: LevelChunkSection) {
        // Blocks
        val wrappedStates = WrappedPalettedContainer(section.states)
        wrappedStates.applyTo(async.blocks)

        // Biomes
        val wrappedBiomes = WrappedPalettedContainer(section.biomes)
        wrappedBiomes.applyTo(async.biomes)
    }
}