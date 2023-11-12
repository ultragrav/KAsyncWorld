package net.ultragrav.kasyncworld.cmd

import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import org.bukkit.Tag
import kotlin.system.measureTimeMillis

class CmdTest3 : SpigotCommand() {
    init {
        addAlias("test3")
    }

    override fun perform() {
        val world = CmdTest.currWorld!!

        val time = measureTimeMillis {
            world.saveAndPack()
        }

        spigotPlayer.sendMessage("Saved and packed in $time ms")
    }
}