package net.ultragrav.kasyncworld.world.inmemory.impl.overrides.task

import io.papermc.paper.chunk.system.io.RegionFileIOThread
import io.papermc.paper.chunk.system.poi.PoiChunk
import io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler
import io.papermc.paper.chunk.system.scheduling.GenericDataLoadTask
import io.papermc.paper.chunk.system.scheduling.NewChunkHolder
import io.papermc.paper.world.ChunkEntitySlices
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.LevelChunkSection
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.ticks.SavedTick
import net.ultragrav.kasyncworld.world.chunk.ChunkHeightOptions
import net.ultragrav.kasyncworld.world.chunk.heightmap.AsyncHeightMap
import net.ultragrav.kasyncworld.world.contract.section.AsyncChunkSection
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.versionio.ChunkReadOptions
import net.ultragrav.kasyncworld.world.versionio.impl.NMSChunkIO
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