package net.ultragrav.kasyncworld.world.inmemory.impl.overrides.task

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor
import io.papermc.paper.chunk.system.poi.PoiChunk
import io.papermc.paper.chunk.system.scheduling.ChunkProgressionTask
import io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler
import io.papermc.paper.chunk.system.scheduling.NewChunkHolder
import io.papermc.paper.world.ChunkEntitySlices
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.*
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.ticks.ProtoChunkTicks
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped.MinecraftPalettedStorage
import net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped.WrappedPalettedContainer
import net.ultragrav.kasyncworld.world.chunk.heightmap.AsyncHeightMap
import net.ultragrav.kasyncworld.world.chunk.heightmap.wrapper.NMSHeightmapStateProvider
import net.ultragrav.kasyncworld.world.chunk.heightmap.wrapper.NMSHeightmapStorageWrapper
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.versionio.impl.NMSChunkIO

class IMChunkLoadTask(
    scheduler: ChunkTaskScheduler,
    world: ServerLevel,
    val holder: NewChunkHolder,
    chunkX: Int,
    chunkZ: Int,
    private val chunkProvider: AsyncChunkProvider
) : ChunkProgressionTask(scheduler, world,
    chunkX,
    chunkZ
) {

    private var scheduled = false

    override fun isScheduled(): Boolean = scheduled

    override fun getTargetStatus(): ChunkStatus = ChunkStatus.EMPTY

    override fun schedule() {
        if (scheduled) throw IllegalStateException("Already scheduled")
        scheduled = true

        val biomesRegistry = world.registryAccess().registryOrThrow(Registries.BIOME)

        holder.pendingEntityChunk = CompoundTag()
        holder.poiChunk = PoiChunk(world, holder.chunkX, holder.chunkZ, world.minSection, world.maxSection)

        val chunk = chunkProvider.loadChunk(chunkX, chunkZ) ?: return run {
            val protoChunk = ProtoChunk(
                ChunkPos(chunkX, chunkZ),
                UpgradeData.EMPTY,
                world,
                biomesRegistry,
                null
            )

            protoChunk.status = ChunkStatus.FULL

            complete(protoChunk, null)
        }

        val chunkSections = chunk.sections
            .map {
                if (it == null) return@map null
                val blocks = it.blocks
                val biomes = it.biomes
                val nmsBlocks = if (blocks is MinecraftPalettedStorage) blocks.wrapped else {
                    val container = PalettedContainer(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES, null)
                    val wrapper = WrappedPalettedContainer(container)
                    blocks.applyTo(wrapper)
                    wrapper.wrapped
                }
                val nmsBiomes = if (biomes is MinecraftPalettedStorage) biomes.wrapped else {
                    val container = PalettedContainer(biomesRegistry.asHolderIdMap(), biomesRegistry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES, null)
                    val wrapper = WrappedPalettedContainer(container)
                    biomes.applyTo(wrapper)
                    wrapper.wrapped
                }
                LevelChunkSection(nmsBlocks, nmsBiomes)
            }
            .toTypedArray()

        val chunkTicksBlock = ProtoChunkTicks<Block>().also {
            chunk.blockTicks.forEach { tick -> it.schedule(tick) }
        }

        val chunkTicksFluid = ProtoChunkTicks<Fluid>().also {
            chunk.fluidTicks.forEach { tick -> it.schedule(tick) }
        }

        val protoChunk = ProtoChunk(
            ChunkPos(chunkX, chunkZ),
            UpgradeData.EMPTY,
            chunkSections,
            chunkTicksBlock,
            chunkTicksFluid,
            world,
            biomesRegistry,
            null
        )

        val baseX = chunkX shl 4
        val baseZ = chunkZ shl 4

        chunk.blockEntities
            .forEach { (pos, nbt) ->
                val copy = nbt.copy()
                copy.putInt("x", pos.x + baseX)
                copy.putInt("y", pos.y)
                copy.putInt("z", pos.z + baseZ)
                protoChunk.setBlockEntityNbt(copy)
            }

        // Write entities to a list tag
        val entitiesListTag = ListTag().apply {
            chunk.entities.map { NMSChunkIO.offsetEntityTag(it.copy(), chunkX, chunkZ) }
                .forEach { entity -> add(entity) }
        }
        val entitiesCompoundTag = CompoundTag().apply {
            put("Entities", entitiesListTag)
        }

        holder.pendingEntityChunk = entitiesCompoundTag

        // Height maps
        chunk.heightMaps.forEach { (type, ahm) ->
            val nmsHm = protoChunk.getOrCreateHeightmapUnprimed(type)
            val wrappedNmsHm = AsyncHeightMap(
                type,
                NMSHeightmapStorageWrapper(nmsHm, protoChunk),
                NMSHeightmapStateProvider(protoChunk),
            )
            ahm.overwrite(wrappedNmsHm)
        }

        // Persistent data
        protoChunk.persistentDataContainer.putAll(chunk.persistentData)

        protoChunk.status = ChunkStatus.FULL
        complete(protoChunk, null)
    }

    override fun cancel() {
        UnsupportedOperationException("Attempted to cancel load task")
            .printStackTrace()
    }

    override fun getPriority(): PrioritisedExecutor.Priority = PrioritisedExecutor.Priority.NORMAL

    override fun lowerPriority(priority: PrioritisedExecutor.Priority?) {}

    override fun setPriority(priority: PrioritisedExecutor.Priority?) {}

    override fun raisePriority(priority: PrioritisedExecutor.Priority?) {}
}