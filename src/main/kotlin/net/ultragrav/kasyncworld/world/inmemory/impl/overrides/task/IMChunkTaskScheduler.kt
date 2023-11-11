package net.ultragrav.kasyncworld.world.inmemory.impl.overrides.task

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor
import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedThreadPool
import io.papermc.paper.chunk.system.ChunkSystem
import io.papermc.paper.chunk.system.scheduling.ChunkHolderManager
import io.papermc.paper.chunk.system.scheduling.ChunkProgressionTask
import io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler
import io.papermc.paper.chunk.system.scheduling.NewChunkHolder
import io.papermc.paper.util.CoordinateUtils
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkStatus
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
                holder.vanillaChunkHolder.onChunkAdd()
                return holder
            }
        }
    }

    override fun createTask(
        chunkX: Int, chunkZ: Int, chunk: ChunkAccess?,
        chunkHolder: NewChunkHolder, neighbours: List<ChunkAccess?>?,
        toStatus: ChunkStatus, initialPriority: PrioritisedExecutor.Priority?
    ): ChunkProgressionTask {
        if (toStatus === ChunkStatus.EMPTY) {
            return IMChunkLoadTask(this, world, chunkHolder, chunkX, chunkZ, worldOptions, chunkProvider)
        }
        return super.createTask(chunkX, chunkZ, chunk, chunkHolder, neighbours, toStatus, initialPriority)
    }
}