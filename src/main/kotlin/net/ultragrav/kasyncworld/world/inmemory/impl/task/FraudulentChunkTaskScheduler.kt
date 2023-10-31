package net.ultragrav.kasyncworld.world.inmemory.impl.task

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor
import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedThreadPool
import io.papermc.paper.chunk.system.scheduling.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkStatus

class FraudulentChunkTaskScheduler(world: ServerLevel, workers: PrioritisedThreadPool?) :
    ChunkTaskScheduler(
        world,
        workers
    ) {
    override fun createTask(
        chunkX: Int, chunkZ: Int, chunk: ChunkAccess?,
        chunkHolder: NewChunkHolder?, neighbours: List<ChunkAccess?>?,
        toStatus: ChunkStatus, initialPriority: PrioritisedExecutor.Priority?
    ): ChunkProgressionTask {
        if (toStatus === ChunkStatus.EMPTY) {
            return AWChunkLoadTask(this, world, chunkX, chunkZ, TODO())
        }
        return super.createTask(chunkX, chunkZ, chunk, chunkHolder, neighbours, toStatus, initialPriority)
    }
}