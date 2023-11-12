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
        val world = spigotPlayer.world

        var total = 0
        val time = measureTimeMillis {
            for (dcx in 0..3) {
                for (dcz in 0..3) {
                    val cx = spigotPlayer.chunk.x + dcx
                    val cz = spigotPlayer.chunk.z + dcz
                    val bc = world.getChunkAt(cx, cz)
                    val chunk = AW.chunkIO.readChunk(bc, AW.storageChunkFactory, ChunkReadOptions())
                    val logTypes = chunk.types()
                        .filter { Tag.LOGS.isTagged(it.bukkitMaterial) }
                    if (logTypes.isEmpty()) continue
                    val count = chunk.sections
                        .filterNotNull()
                        .sumOf { section ->
                            logTypes.sumOf {
                                section.blocks.count(it)
                            }
                        }
                    total += count
                }
            }
        }

        spigotPlayer.sendMessage("Found $total logs in ${time}ms")
    }
}