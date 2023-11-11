package net.ultragrav.kasyncworld.cmd

import net.kyori.adventure.text.Component
import net.minecraft.world.level.block.Blocks
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.getSectionIndexMB
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.inmemory.InMemoryWorldOptions
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
        val radius = 8

        val pl = spigotPlayer
        val plChunkX = pl.location.chunk.x
        val plChunkZ = pl.location.chunk.z

        AW.editAsync(spigotPlayer.world, AsyncWorld.EditType.DENSE) {
            val state = Blocks.AIR.defaultBlockState()
            for (dx in -radius..radius) {
                for (dz in -radius..radius) {
                    val cx = plChunkX + dx
                    val cz = plChunkZ + dz
                    val chunk = getChunk(cx, cz)
                    chunk.sections.indices.forEach { index ->
                        val section = chunk.createSection()
                        for (i in 0 until section.blocks.size) {
                            section.blocks[i] = state
                        }
                        chunk.setSection(chunk.heightOptions.getSectionIndexMB(index), section)
                    }
                }
            }
        }.thenAccept {
            pl.sendMessage(Component.text("Done!"))
        }
    }

}