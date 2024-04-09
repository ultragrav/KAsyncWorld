package net.ultragrav.kasyncworld.world.inmemory.impl

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.LevelSettings
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.storage.PrimaryLevelData
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.inmemory.IMWorldProvider
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorld
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions
import net.ultragrav.kasyncworld.world.inmemory.chunk.AsyncChunkProvider
import net.ultragrav.kasyncworld.world.inmemory.impl.overrides.IMServerLevel
import net.ultragrav.kasyncworld.world.inmemory.pack.PackedWorld
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World.Environment
import org.bukkit.event.world.WorldLoadEvent
import kotlin.time.measureTimedValue

object PaperIMWorldProvider : IMWorldProvider {
    private fun createWorld(
        name: String,
        options: InMemoryWorldOptions,
        environment: Environment,
        chunkProvider: AsyncChunkProvider
    ): ServerLevel {
        val worldOptions = WorldOptions(options.seed, true, false)

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

        val (serverLevel, time) = measureTimedValue {
            IMServerLevel(
                name,
                chunkProvider,
                levelData,
                levelKey,
                levelDimension,
                levelStem,
                options.seed,
                environment,
                null,
                options
            )
        }

        AW.debug("Instantiated world $name in ${time.inWholeMilliseconds}ms")

        if (Bukkit.getServer().getWorld(name) == null) {
            throw IllegalStateException("World $name was not loaded by Paper")
        }

        mcServer.addLevel(serverLevel)
        levelData.setSpawn(BlockPos.ZERO.above(80), 0.0f)
        levelData.isInitialized = true
        mcServer.initWorld(serverLevel, levelData, levelData, worldOptions)

        serverLevel.keepSpawnInMemory = false
        serverLevel.kasyncDebug = false
        serverLevel.setSpawnSettings(true, true)
        mcServer.prepareLevels(serverLevel.chunkSource.chunkMap.progressListener, serverLevel)

        Bukkit.getPluginManager().callEvent(WorldLoadEvent(serverLevel.world))

        return serverLevel
    }

    override fun createWorld(name: String, options: InMemoryWorldOptions): InMemoryWorld {

        check(Bukkit.isPrimaryThread()) {
            "Worlds must be created on the main thread"
        }

        val chunkProvider = BasicChunkProvider(
            AW.storageChunkFactory,
            options.codec,
            options.chunkBoundsX,
            options.chunkBoundsZ
        )

        val serverLevel = createWorld(name, options, options.environment, chunkProvider)

        return PaperMemoryWorld(serverLevel, name, options, chunkProvider)
    }

    override fun createWorld(name: String, options: InMemoryWorldOptions, packed: PackedWorld): InMemoryWorld {

        check(Bukkit.isPrimaryThread()) {
            "Worlds must be created on the main thread"
        }

        val chunkProvider = BasicChunkProvider(
            AW.storageChunkFactory,
            options.codec,
            options.chunkBoundsX,
            options.chunkBoundsZ
        )

        chunkProvider.setChunks(packed.chunks)

        val (serverLevel, time) = measureTimedValue {
            createWorld(name, options, options.environment, chunkProvider)
        }

        AW.debug("World $name created in ${time.inWholeMilliseconds}ms")

        return PaperMemoryWorld(serverLevel, name, options, chunkProvider)

    }
}