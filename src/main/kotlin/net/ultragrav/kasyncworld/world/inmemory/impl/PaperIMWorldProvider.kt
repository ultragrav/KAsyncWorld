package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Difficulty
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.LevelSettings
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.storage.PrimaryLevelData
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.inmemory.*
import net.ultragrav.kasyncworld.world.inmemory.impl.overrides.IMServerLevel
import net.ultragrav.kasyncworld.world.inmemory.impl.overrides.PaperMemoryWorld
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World.Environment
import org.bukkit.event.world.WorldLoadEvent

class PaperIMWorldProvider : IMWorldProvider {

    private fun createWorld(
        name: String,
        options: InMemoryWorldOptions,
        seed: Long,
        environment: Environment,
        chunkProvider: AsyncChunkProvider
    ): ServerLevel {
        val worldOptions = WorldOptions(seed, true, false)

        val mcServer = MinecraftServer.getServer() as DedicatedServer

        val levelKey = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation(
                "midnightsky",
                name.lowercase().replace(" ", "_")
            )
        )

        require(Bukkit.getWorld(name) == null) {
            "World $name already exists"
        }
        require(Bukkit.getWorld(NamespacedKey("midnightsky", name.lowercase().replace(" ", "_"))) == null) {
            "World $name already exists"
        }

        val levelSettings = LevelSettings(
            name,
            GameType.DEFAULT_MODE,
            false,
            Difficulty.NORMAL,
            true,
            GameRules(),
            mcServer.worldLoader.dataConfiguration
        )

        val dedicatedProperties = mcServer.properties
        var levelStemRegistry = mcServer.worldLoader.datapackDimensions.registryOrThrow(Registries.LEVEL_STEM)
        val createDimensions = dedicatedProperties.createDimensions(mcServer.worldLoader.datapackWorldgen)
        val baked = createDimensions.bake(levelStemRegistry)
        levelStemRegistry = baked.dimensions
        val lifecycle = baked.lifecycle().add(mcServer.worldLoader.datapackWorldgen.allRegistriesLifecycle())

        val levelData = PrimaryLevelData(levelSettings, worldOptions, baked.specialWorldProperty, lifecycle)
        levelData.customDimensions = levelStemRegistry
        levelData.checkName(name)
        levelData.setModdedInfo(mcServer.serverModName, mcServer.moddedStatus.shouldReportAsModified())

        val levelDimension = when (environment) {
            Environment.NORMAL -> LevelStem.OVERWORLD
            Environment.NETHER -> LevelStem.NETHER
            Environment.THE_END -> LevelStem.END
            else -> throw IllegalArgumentException("Invalid environment $environment")
        }

        val levelStem = levelStemRegistry.getOrThrow(levelDimension)

        val serverLevel = IMServerLevel(
            name,
            chunkProvider,
            levelData,
            levelKey,
            levelDimension,
            levelStem,
            seed,
            environment,
            null,
            options
        )

        if (Bukkit.getServer().getWorld(name) == null) {
            throw IllegalStateException("World $name was not loaded by Paper")
        }

        mcServer.addLevel(serverLevel)
        mcServer.initWorld(serverLevel, levelData, levelData, worldOptions)

        serverLevel.keepSpawnInMemory = false
        serverLevel.kasyncDebug = false
        serverLevel.setSpawnSettings(true, true)
        mcServer.prepareLevels(serverLevel.chunkSource.chunkMap.progressListener, serverLevel)

        Bukkit.getPluginManager().callEvent(WorldLoadEvent(serverLevel.world))

        return serverLevel
    }

    override fun createWorld(name: String, options: InMemoryWorldOptions): InMemoryWorld {

        val chunkProvider =
            if (options.compressUnloadedChunks) {
                CompressedChunkProvider(
                    AW.storageChunkFactory,
                    options.codec,
                    options.chunkBoundsX,
                    options.chunkBoundsZ
                )
            } else {
                UncompressedChunkProvider(
                    AW.storageChunkFactory,
                    options.codec,
                    options.chunkBoundsX,
                    options.chunkBoundsZ
                )
            }

        val serverLevel = createWorld(name, options, 123L, options.environment, chunkProvider)

        return PaperMemoryWorld(serverLevel, name, options, chunkProvider)
    }

    override fun createWorld(name: String, options: InMemoryWorldOptions, packed: PackedWorld): InMemoryWorld {
        val chunkProvider =
            if (options.compressUnloadedChunks) {
                CompressedChunkProvider(
                    AW.storageChunkFactory,
                    options.codec,
                    options.chunkBoundsX,
                    options.chunkBoundsZ
                )
            } else {
                UncompressedChunkProvider(
                    AW.storageChunkFactory,
                    options.codec,
                    options.chunkBoundsX,
                    options.chunkBoundsZ
                )
            }

        chunkProvider.setChunks(
            packed.chunks.associateBy { ChunkPos(it.x, it.z) }.mapValues { it.value.chunk }
        )

        val serverLevel = createWorld(name, options, 123L, options.environment, chunkProvider)


        return PaperMemoryWorld(serverLevel, name, options, chunkProvider)

    }
}