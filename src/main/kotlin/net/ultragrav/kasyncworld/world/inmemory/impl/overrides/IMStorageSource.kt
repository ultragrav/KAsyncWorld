package net.ultragrav.kasyncworld.world.inmemory.impl.overrides

import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Dynamic
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.server.commands.TagCommand
import net.minecraft.util.datafix.DataFixers
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraft.world.level.storage.LevelSummary
import net.minecraft.world.level.storage.PlayerDataStorage
import net.minecraft.world.level.storage.WorldData
import net.minecraft.world.level.validation.DirectoryValidator
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private object Pre {
    val file = Files.createTempDirectory("kasyncworld-${UUID.randomUUID()}")
    init {
        file.toFile().deleteOnExit()
    }
}

object IMStorageSource : LevelStorageSource(
    Pre.file.toAbsolutePath(),
    Pre.file.toAbsolutePath(),
    DirectoryValidator { false },
    DataFixers.getDataFixer()
) {
    override fun validateAndCreateAccess(s: String, dimensionType: ResourceKey<LevelStem>): LevelStorageAccess {
        return super.validateAndCreateAccess(s, dimensionType)
    }

    class IMStorageAccess(levelId: String, path: Path, dimensionType: ResourceKey<LevelStem>) : LevelStorageAccess(levelId, path,
        dimensionType
    ) {
        override fun createPlayerStorage(): PlayerDataStorage {
            return IMPlayerStorage(this, DataFixers.getDataFixer())
        }

        override fun deleteLevel() {}
        override fun getIconFile(): Optional<Path> = Optional.empty()
        override fun renameLevel(name: String) {}
        override fun makeWorldBackup(): Long = 0L
        override fun saveDataTag(registryManager: RegistryAccess, saveProperties: WorldData) {}
        override fun saveDataTag(registryManager: RegistryAccess, saveProperties: WorldData, nbt: CompoundTag?) {}
        override fun getSummary(dynamic: Dynamic<*>): LevelSummary = throw UnsupportedOperationException()

        override fun getDataTag(): Dynamic<*> = Dynamic(NbtOps.INSTANCE, CompoundTag())

    }

    class IMPlayerStorage(session: LevelStorageAccess, dataFixer: DataFixer) : PlayerDataStorage(session, dataFixer) {
        override fun load(name: String, uuid: String): Optional<CompoundTag> = Optional.empty()
        override fun save(player: Player) {}
    }
}