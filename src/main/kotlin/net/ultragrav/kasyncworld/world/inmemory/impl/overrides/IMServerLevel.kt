package net.ultragrav.kasyncworld.world.inmemory.impl.overrides

import com.google.common.collect.ImmutableList
import io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ProgressListener
import net.minecraft.world.RandomSequences
import net.minecraft.world.entity.ai.village.VillageSiege
import net.minecraft.world.entity.npc.CatSpawner
import net.minecraft.world.entity.npc.WanderingTraderSpawner
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.PatrolSpawner
import net.minecraft.world.level.levelgen.PhantomSpawner
import net.minecraft.world.level.storage.PrimaryLevelData
import net.ultragrav.kasyncworld.world.inmemory.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.impl.overrides.task.IMChunkTaskScheduler
import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator

class IMServerLevel(
    name: String,
    val chunkProvider: AsyncChunkProvider,
    levelData: PrimaryLevelData,
    levelKey: ResourceKey<Level>,
    dimensionKey: ResourceKey<LevelStem>,
    dimension: LevelStem,
    seed: Long,
    env: World.Environment,
    gen: ChunkGenerator?,
) : ServerLevel(
    MinecraftServer.getServer(),
    MinecraftServer.getServer().executor,
    IMStorageSource.createAccess(name, dimensionKey),
    levelData,
    levelKey,
    dimension,
    MinecraftServer.getServer().progressListenerFactory.create(11),
    false,
    seed,
    if (env == World.Environment.NORMAL) listOf(
        PhantomSpawner(),
        PatrolSpawner(),
        CatSpawner(),
        VillageSiege(),
        WanderingTraderSpawner(levelData)
    ) else listOf(),
    true,
    null,
    env,
    gen,
    null // Why warning?
) {

    override fun createChunkTaskScheduler(): ChunkTaskScheduler {
        return IMChunkTaskScheduler(chunkProvider, this, ChunkTaskScheduler.workerThreads)
    }

    override fun save(progressListener: ProgressListener?, flush: Boolean, savingDisabled: Boolean) {}
    override fun save(progressListener: ProgressListener?, flush: Boolean, savingDisabled: Boolean, close: Boolean) {}
}