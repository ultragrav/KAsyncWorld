package net.ultragrav.kasyncworld.cmd

import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions
import net.ultragrav.kasyncworld.world.inmemory.PackedWorld
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Biome
import kotlin.system.measureTimeMillis

class CmdTest2 : SpigotCommand() {
    init {
        addAlias("test2")
    }

    override fun perform() {
        val imw = CmdTest.currWorld ?: return

        spigotPlayer.teleport(Location(Bukkit.getWorld("World"), 0.0, 80.0, 0.0))

        val packed = PackedWorld(listOf())
        val ms = measureTimeMillis {
            imw.unload(false)
            imw.saveAndPack()
        }

        tell("Saved in $ms ms")

        CmdTest.currWorld = AW.inMemoryWorldProvider.createWorld(
            "Test-World-${System.currentTimeMillis()}",
            InMemoryWorldOptions(
                0..3,
                0..3,
                World.Environment.NETHER,
                Biome.CRIMSON_FOREST,
                true,
                AW.codec
            ),
            packed
        )

        val location = Location(
            CmdTest.currWorld!!.bukkitWorld,
            0.0,
            80.0,
            0.0
        )

        spigotPlayer.teleport(location)
    }

}