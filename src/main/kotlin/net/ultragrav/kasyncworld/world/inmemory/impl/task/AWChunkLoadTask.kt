package net.ultragrav.kasyncworld.world.inmemory.impl.task

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor
import io.papermc.paper.chunk.system.scheduling.ChunkProgressionTask
import io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.*
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.ticks.ProtoChunkTicks
import net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped.MinecraftPalettedStorage
import net.ultragrav.kasyncworld.world.chunk.block.storage.wrapped.WrappedPalettedContainer
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider

class AWChunkLoadTask(
    scheduler: ChunkTaskScheduler,
    world: ServerLevel,
    chunkX: Int,
    chunkZ: Int,
    val chunkProvider: AsyncChunkProvider
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

        val chunk = chunkProvider.loadChunk(chunkX, chunkZ) ?: return run {
            val protoChunk = ProtoChunk(
                ChunkPos(chunkX, chunkZ),
                UpgradeData.EMPTY,
                world,
                biomesRegistry,
                null
            )

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
                    wrapper.copyFrom(blocks)
                    wrapper.wrapped
                }
                val nmsBiomes = if (biomes is MinecraftPalettedStorage) biomes.wrapped else {
                    val container = PalettedContainer(biomesRegistry.asHolderIdMap(), biomesRegistry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES, null)
                    val wrapper = WrappedPalettedContainer(container)
                    wrapper.copyFrom(biomes)
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