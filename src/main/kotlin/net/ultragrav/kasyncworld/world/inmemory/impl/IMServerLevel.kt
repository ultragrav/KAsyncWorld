package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.world.RandomSequences
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraft.world.level.storage.PrimaryLevelData
import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import java.util.concurrent.Executor

class IMServerLevel(minecraftserver: MinecraftServer, executor: Executor,
                    convertable_conversionsession: LevelStorageSource.LevelStorageAccess,
                    iworlddataserver: PrimaryLevelData, resourcekey: ResourceKey<Level>, worlddimension: LevelStem,
                    worldloadlistener: ChunkProgressListener, flag: Boolean, i: Long, list: MutableList<CustomSpawner>,
                    flag1: Boolean, randomsequences: RandomSequences?, env: World.Environment, gen: ChunkGenerator,
                    biomeProvider: BiomeProvider
) : ServerLevel(minecraftserver, executor, convertable_conversionsession, iworlddataserver, resourcekey, worlddimension,
    worldloadlistener, flag, i, list, flag1, randomsequences, env, gen, biomeProvider
) {
}