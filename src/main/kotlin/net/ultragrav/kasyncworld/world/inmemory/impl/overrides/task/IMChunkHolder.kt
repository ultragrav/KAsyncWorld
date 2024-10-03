package net.ultragrav.kasyncworld.world.inmemory.impl.overrides.task

import ca.spottedleaf.moonrise.patches.chunk_system.io.RegionFileIOThread
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices
import ca.spottedleaf.moonrise.patches.chunk_system.level.poi.PoiChunk
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkTaskScheduler
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.task.GenericDataLoadTask
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.chunk.ChunkAccess
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.inmemory.chunk.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import net.ultragrav.kasyncworld.world.chunk.io.impl.NMSChunkIO
import java.util.function.Consumer

class IMChunkHolder(
    world: ServerLevel,
    chunkX: Int,
    chunkZ: Int,
    scheduler: ChunkTaskScheduler,
    val chunkProvider: AsyncChunkProvider
) : NewChunkHolder(world, chunkX, chunkZ,
    scheduler
) {

    init {
        pendingEntityChunk = CompoundTag()
    }

    override fun saveChunk(chunk: ChunkAccess, unloading: Boolean): Boolean {

        if (chunkX in chunkProvider.boundsX && chunkZ in chunkProvider.boundsZ) {
            val heightOptions = ChunkHeightOptions(chunk.sections.size, chunk.minSection)
            val asyncChunk = chunkProvider.factory.createChunk(heightOptions)
            NMSChunkIO.readChunk(world, chunk, entityChunk, asyncChunk, ChunkReadOptions())
            chunkProvider.storeChunk(chunkX, chunkZ, asyncChunk)
        }

        getUnloadTask(RegionFileIOThread.RegionFileType.CHUNK_DATA)
            ?.completable
            ?.complete(null)

        return true
    }

    override fun isEntityChunkNBTLoaded(): Boolean {
        if (pendingEntityChunk == null) pendingEntityChunk = CompoundTag()
        return true
    }

    override fun isPoiChunkLoaded(): Boolean {
        if (poiChunk == null) poiChunk = PoiChunk(world, chunkX, chunkZ, world.minSection, world.maxSection)
        return true
    }

    override fun saveEntities(entities: ChunkEntitySlices?, unloading: Boolean): Boolean {
        // Do absolutely nothing, entities are saved in #saveChunk, and the task is completed
        // in a different place
        return false
    }

    override fun savePOI(poi: PoiChunk?, unloading: Boolean): Boolean {
        // Do absolutely nothing, POIs are never saved
        getUnloadTask(RegionFileIOThread.RegionFileType.POI_DATA)
            ?.completable
            ?.complete(null)
        return false
    }

    override fun getOrLoadEntityData(consumer: Consumer<GenericDataLoadTask.TaskResult<CompoundTag, Throwable>>?): GenericDataLoadTaskCallback {
        throw UnsupportedOperationException("Not implemented")
    }

    override fun getOrLoadPoiData(consumer: Consumer<GenericDataLoadTask.TaskResult<PoiChunk, Throwable>>?): GenericDataLoadTaskCallback {
        throw UnsupportedOperationException("Not implemented")
    }

}