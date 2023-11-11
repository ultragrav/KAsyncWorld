package net.ultragrav.kasyncworld.cmd

import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import kotlin.system.measureTimeMillis

class CmdTest3 : SpigotCommand() {
    init {
        addAlias("test3")
    }

    override fun perform() {
        val world = spigotPlayer.world
        val time = measureTimeMillis {
            for (dcx in 0..3) {
                for (dcz in 0..3) {
                    val cx = spigotPlayer.chunk.x + dcx
                    val cz = spigotPlayer.chunk.z + dcz
                    val chunk = AW.chunkIO.readChunk(world.getChunkAt(cx, cz), AW.storageChunkFactory, ChunkReadOptions())

                }
            }
        }
    }
}