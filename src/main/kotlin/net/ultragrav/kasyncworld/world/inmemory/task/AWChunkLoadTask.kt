package net.ultragrav.kasyncworld.world.inmemory.task

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
                val statesContainer = PalettedContainer(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES, null)
                val biomesContainer = PalettedContainer(biomesRegistry.asHolderIdMap(), biomesRegistry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES, null)
                TODO("Copy data from things")
                LevelChunkSection(statesContainer, biomesContainer)
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