package net.ultragrav.kasyncworld.world.inmemory.impl.overrides.task

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor
import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedThreadPool
import ca.spottedleaf.moonrise.common.util.ChunkSystem
import ca.spottedleaf.moonrise.common.util.CoordinateUtils
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkHolderManager
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkTaskScheduler
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.task.ChunkProgressionTask
import net.minecraft.server.level.GenerationChunkHolder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.StaticCache2D
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.ultragrav.kasyncworld.world.inmemory.chunk.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions

class IMChunkTaskScheduler(world: ServerLevel, workers: PrioritisedThreadPool?) :
    ChunkTaskScheduler(
        world,
        workers
    ) {

    lateinit var worldOptions: InMemoryWorldOptions
    lateinit var chunkProvider: AsyncChunkProvider

    init {
        this.chunkHolderManager = object : ChunkHolderManager(world, this) {
            override fun createChunkHolder(position: Long): NewChunkHolder {
                val holder = IMChunkHolder(
                    world,
                    CoordinateUtils.getChunkX(position),
                    CoordinateUtils.getChunkZ(position),
                    this@IMChunkTaskScheduler,
                    chunkProvider
                )

                ChunkSystem.onChunkHolderCreate(world, holder.vanillaChunkHolder)
                return holder
            }
        }
    }

    override fun createTask(
        chunkX: Int,
        chunkZ: Int,
        chunk: ChunkAccess,
        chunkHolder: NewChunkHolder,
        neighbours: StaticCache2D<GenerationChunkHolder>?,
        toStatus: ChunkStatus?,
        initialPriority: PrioritisedExecutor.Priority?
    ): ChunkProgressionTask? {
        if (toStatus === ChunkStatus.EMPTY) {
            return IMChunkLoadTask(this, world, chunkHolder, chunkX, chunkZ, worldOptions, chunkProvider)
        }
        return super.createTask(chunkX, chunkZ, chunk, chunkHolder, neighbours, toStatus, initialPriority)
    }
}