package net.ultragrav.kasyncworld.world.chunk.io.impl

import ca.spottedleaf.starlight.common.light.StarLightEngine
import io.papermc.paper.world.ChunkEntitySlices
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunkSection
import net.minecraft.world.level.chunk.PalettedContainer
import net.minecraft.world.ticks.ProtoChunkTicks
import net.minecraft.world.ticks.SavedTick
import net.ultragrav.kasyncworld.entityPosition
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.block.bit.BitStorage
import net.ultragrav.kasyncworld.world.chunk.block.position.AWBlockPosition
import net.ultragrav.kasyncworld.world.chunk.block.storage.PalettedStorageImpl
import net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped.WrappedPalettedContainer
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory
import net.ultragrav.kasyncworld.world.chunk.contract.section.AsyncChunkSection
import net.ultragrav.kasyncworld.world.chunk.getSectionIndexMB
import net.ultragrav.kasyncworld.world.chunk.heightmap.AsyncHeightMap
import net.ultragrav.kasyncworld.world.chunk.heightmap.wrapper.NMSHeightmapStateProvider
import net.ultragrav.kasyncworld.world.chunk.heightmap.wrapper.NMSHeightmapStorageWrapper
import net.ultragrav.kasyncworld.world.chunk.io.ChunkIO
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import net.ultragrav.kasyncworld.world.chunk.io.ChunkWriteOptions
import net.ultragrav.kasyncworld.world.chunk.io.HeightmapWriteType
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld

class NMSChunkIO : ChunkIO {

    override fun writeChunk(bukkitChunk: Chunk, chunk: AsyncChunk, options: ChunkWriteOptions) {

        val nms = (bukkitChunk.world as CraftWorld)
            .handle
            .chunkSource
            .getChunkAtIfLoadedImmediately(bukkitChunk.x, bukkitChunk.z)
            ?: throw IllegalStateException("Chunk not loaded")

        val cx = nms.locX
        val cz = nms.locZ

        // Sections (Blocks)
        for (i in 0..<nms.sectionsCount) {
            val section = chunk.sections[i] ?: continue
            val nmsSection = nms.sections[i] ?: continue
            nms.sections[i] = nmsSection
            writeSection(section, nmsSection)
            nmsSection.recalcBlockCounts()
        }

        fun wasBlockEdited(pos: BlockPos): Boolean {
            val sectionIndexMB = pos.y shr 4
            val section = chunk.getSection(sectionIndexMB) ?: return false
            val sectionX = pos.x and 15
            val sectionY = pos.y and 15
            val sectionZ = pos.z and 15
            val blockIndex = section.getBlockIndex(sectionX, sectionY, sectionZ)
            return blockIndex in section.blocks.iterationStrategy
        }

        // This method might be run chunk-wise-parallel, so we need to make sure
        // that parts that interact with the level are synchronized
        synchronized(this) {
            // Remove existing block entities
            nms.blockEntities
                .mapKeys { BlockPos(it.key.x - (cx shl 4), it.key.y, it.key.z - (cz shl 4)) }
                .toList()
                .forEach { (pos, _) ->
                    val wasBlockSet = wasBlockEdited(pos)
                    val isTileSet =
                        AWBlockPosition(pos.x, pos.y, pos.z) in chunk.blockEntities
                    if (!wasBlockSet && !isTileSet) return@forEach

                    val blockPos = BlockPos(
                        pos.x + (cx shl 4),
                        pos.y,
                        pos.z + (cz shl 4)
                    )

                    try {
                        nms.removeBlockEntity(blockPos)
                    } catch (e: java.lang.IllegalStateException) {
                        if (e.message?.contains("triggered") == true) {
                            // async event call
                            // ignore
                            // We do need to retry, this time it will succeed
                            nms.removeBlockEntity(blockPos)
                        } else {
                            throw e
                        }
                    }
                }

            // Add new ones
            val baseX = cx shl 4
            val baseZ = cz shl 4
            chunk.blockEntities.forEach { (pos, tag) ->
                val nmsPos = BlockPos(pos.x + baseX, pos.y, pos.z + baseZ)
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
                        if (it.type != org.bukkit.entity.EntityType.PLAYER) {
                            it.remove()
                        }
                    }
            }

            val entities = chunk.entities.map { offsetEntityTag(it.copy(), cx, cz) }
            val decodedEntities = EntityType.loadEntitiesRecursive(entities, nms.level).toList()
            nms.level.entityLookup.addEntityChunkEntities(decodedEntities, ChunkPos(nms.locX, nms.locZ))

        }

        // Persistent Data
        if (options.writePersistentContainer) {
            nms.persistentDataContainer.clear()
            nms.persistentDataContainer.putAll(chunk.persistentData)
        }

        // Fluid and Block Ticks
        val blockTicks = chunk.blockTicks
        nms.blockTicks.removeIf { wasBlockEdited(it.pos) }
        blockTicks.forEachIndexed { index, it ->
            nms.blockTicks.schedule(
                offsetTick(it, cx, cz).unpack(
                    nms.level.gameTime,
                    (index - blockTicks.size).toLong()
                )
            )
        }

        val fluidTicks = chunk.fluidTicks
        nms.fluidTicks.removeIf { wasBlockEdited(it.pos) }
        fluidTicks.forEachIndexed { index, it ->
            nms.fluidTicks.schedule(
                offsetTick(it, cx, cz).unpack(
                    nms.level.gameTime,
                    (index - fluidTicks.size).toLong()
                )
            )
        }

        // Heightmaps
        nms.heightmaps.forEach { (key, wrapped) ->
            val wrapper = AsyncHeightMap(
                key,
                NMSHeightmapStorageWrapper(wrapped, nms),
                NMSHeightmapStateProvider(nms)
            )
            val hm = chunk.heightMaps[key] ?: run {
                wrapper.recalculate()
                return@forEach
            }

            when (options.heightmapWriteType) {
                HeightmapWriteType.RECALCULATE -> wrapper.recalculate()
                HeightmapWriteType.OVERWRITE -> hm.overwrite(wrapper)
                HeightmapWriteType.MERGE -> wrapper.editWith(hm)
            }
        }
        if (options.relight) {
            nms.isLightCorrect = false
            val emptySections = StarLightEngine.getEmptySectionsForChunk(nms)
            nms.level.chunkSource.lightEngine.theLightEngine.lightChunk(nms, emptySections)
            nms.isLightCorrect = true
        }

        if (options.sendPackets) {
            sendPackets(nms.level.world, nms.locX, nms.locZ)
        }


    }

    private fun writeSection(section: AsyncChunkSection, nmsSection: LevelChunkSection) {
        // Blocks
        val wrappedStates = WrappedPalettedContainer(nmsSection.states)
        section.blocks.applyTo(wrappedStates)

        // Biomes
        val wrappedBiomes = WrappedPalettedContainer(nmsSection.biomes as PalettedContainer<Holder<Biome>>)
        section.biomes.applyTo(wrappedBiomes)
    }

    override fun sendPackets(world: World, cx: Int, cz: Int) {
        world.refreshChunk(cx, cz)
    }

    override fun readChunk(bukkitChunk: Chunk, factory: AsyncChunkFactory, options: ChunkReadOptions): AsyncChunk {
        val nms = (bukkitChunk.world as CraftWorld)
            .handle
            .chunkSource
            .getChunkAtIfLoadedImmediately(bukkitChunk.x, bukkitChunk.z)
            ?: throw IllegalStateException("Chunk is not loaded")
        val height = ChunkHeightOptions(nms.sectionsCount, nms.minSection)
        val chunk = factory.createChunk(height)

        val cx = nms.locX
        val cz = nms.locZ

        // Chunk-wise parallelism is not possible here as we are
        // using EntityLookup, which is a shared resource.
        val entityChunk = synchronized(this) {
            nms.level.entityLookup.getChunk(cx, cz)
        }

        readChunk(nms.level, nms, entityChunk, chunk, options)

        return chunk
    }

    companion object {
        fun readChunk(
            level: ServerLevel,
            nms: ChunkAccess,
            entityChunk: ChunkEntitySlices?,
            chunk: AsyncChunk,
            options: ChunkReadOptions
        ) {

            val cx = nms.locX
            val cz = nms.locZ

            // Sections (Blocks)
            if (options.readBlocks || options.readBiomes) {
                for (i in 0..<nms.sectionsCount) {
                    val sectionIndexMB = chunk.heightOptions.getSectionIndexMB(i)
                    if (sectionIndexMB !in options.sectionMask) continue
                    val nmsSection = nms.sections[i] ?: continue
                    val section = chunk.createSection()
                    readSection(section, nmsSection, options.readBlocks, options.readBiomes)
                    chunk.setSection(chunk.heightOptions.getSectionIndexMB(i), section)
                }
            }

            // Block Entities
            if (options.readBlockEntities) {
                nms.blockEntities.mapValues { it.value.saveWithFullMetadata() }
                    .filterKeys { it.y shr 4 in options.sectionMask }
                    .forEach { (pos, ent) -> chunk.setBlockEntity(pos.x and 0xF, pos.y, pos.z and 0xF, ent) }
            }

            // Entities
            if (options.readEntities && entityChunk != null) {
                val save = entityChunk.save()
                if (save != null) {
                    val listTag = save.getList("Entities", Tag.TAG_COMPOUND.toInt())
                    listTag.filterIsInstance<CompoundTag>()
                        .filter { (it.entityPosition().y.toInt() shr 4) in options.sectionMask }
                        .map { relativizeEntityTag(it, cx, cz) }
                        .forEach { chunk.addEntity(it) }
                }
            }

            // Persistent data
            if (options.readPersistentContainer) {
                chunk.persistentData = nms.persistentDataContainer.toTagCompound()
            }

            if (options.readTicks) {
                // Block/Fluid ticks
                val ticksForSerialization = nms.ticksForSerialization

                val blockTicksTag = ticksForSerialization.blocks.save(level.gameTime) {
                    BuiltInRegistries.BLOCK.getKey(it).toString()
                } as ListTag

                val fluidTicksTag = ticksForSerialization.fluids.save(level.gameTime) {
                    BuiltInRegistries.FLUID.getKey(it).toString()
                } as ListTag

                val blockTicks = ProtoChunkTicks.load(blockTicksTag, {
                    BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(it))
                }, ChunkPos(cx, cz))

                val fluidTicks = ProtoChunkTicks.load(fluidTicksTag, {
                    BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(it))
                }, ChunkPos(cx, cz))

                chunk.blockTicks = blockTicks.scheduledTicks()
                    .filter { it.pos.y shr 4 in options.sectionMask }
                    .map { relativeTick(it) }
                    .toMutableList()
                chunk.fluidTicks = fluidTicks.scheduledTicks()
                    .filter { it.pos.y shr 4 in options.sectionMask }
                    .map { relativeTick(it) }
                    .toMutableList()
            }

            if (options.readHeightmaps) {
                // Height Maps
                nms.heightmaps.forEach { (type, map) ->
                    val newHeightMap = AsyncHeightMap(
                        type,
                        chunk
                    )

                    val wrapper = AsyncHeightMap(
                        type,
                        NMSHeightmapStorageWrapper(map, nms),
                        NMSHeightmapStateProvider(nms)
                    )

                    wrapper.overwrite(newHeightMap)

                    chunk.setHeightMap(type, newHeightMap)
                }
            }
        }

        private fun <T> relativeTick(tick: SavedTick<T>): SavedTick<T> {
            return SavedTick(
                tick.type,
                BlockPos(tick.pos.x and 0xF, tick.pos.y, tick.pos.z and 0xF),
                tick.delay,
                tick.priority
            )
        }

        fun <T> offsetTick(tick: SavedTick<T>, cx: Int, cz: Int): SavedTick<T> {
            val offsetX = cx shl 4
            val offsetZ = cz shl 4
            return SavedTick(
                tick.type,
                BlockPos(tick.pos.x + offsetX, tick.pos.y, tick.pos.z + offsetZ),
                tick.delay,
                tick.priority
            )
        }

        fun relativizeEntityTag(tag: CompoundTag, cx: Int, cz: Int): CompoundTag {
            val pos = tag.getList("Pos", Tag.TAG_DOUBLE.toInt())
            val currX = pos.getDouble(0)
            val currZ = pos.getDouble(2)
            pos[0] = DoubleTag.valueOf(currX - (cx shl 4))
            pos[2] = DoubleTag.valueOf(currZ - (cz shl 4))
            return tag
        }

        fun offsetEntityTag(tag: CompoundTag, cx: Int, cz: Int): CompoundTag {
            val pos = tag.getList("Pos", Tag.TAG_DOUBLE.toInt())
            val currX = pos.getDouble(0)
            val currZ = pos.getDouble(2)
            pos[0] = DoubleTag.valueOf(currX + (cx shl 4))
            pos[2] = DoubleTag.valueOf(currZ + (cz shl 4))
            return tag
        }

        private fun readSection(
            async: AsyncChunkSection,
            section: LevelChunkSection,
            readBlocks: Boolean,
            readBiomes: Boolean
        ) {
            // Blocks
            if (readBlocks && !section.hasOnlyAir()) {
                val blocks = async.blocks
                if (blocks is PalettedStorageImpl<BlockState> && blocks.storage is BitStorage) {
                    // Fast algo
                    val bits = section.states.data.storage.bits
                    blocks.palette = blocks.config.createPalette()
                    blocks.storage = blocks.config.createStorage(bits.coerceAtLeast(1))
                    blocks.iterationStrategy = blocks.config.createIterationStrategy()
                    blocks.iterationStrategy.setAll()

                    val storage = blocks.storage as BitStorage

                    // Transfer palette
                    (0..<section.states.data.palette.size)
                        .forEach { id ->
                            val state = section.states.data.palette.valueFor(id)
                            check(id == blocks.palette.getId(state)) { "Palette mismatch" }
                        }

                    // Transfer longs
                    if (bits != 0) {
                        val raw = section.states.data.storage.raw.copyOf()
                        storage.useRaw(raw)
                    }

                    // Recount
                    blocks.recount()
                } else {
                    // Slower algo
                    val wrappedStates = WrappedPalettedContainer(section.states)
                    wrappedStates.applyTo(async.blocks)
                }
            }

            if (readBiomes) {
                // Biomes
                val wrappedBiomes = WrappedPalettedContainer(section.biomes as PalettedContainer<Holder<Biome>>)
                wrappedBiomes.applyTo(async.biomes)
            }
        }
    }
}