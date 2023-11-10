package net.ultragrav.kasyncworld.cmd

import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.versionio.ChunkWriteOptions
import kotlin.system.measureNanoTime

class CmdPasteChunk : SpigotCommand() {
    init {
        addAlias("pastechunk")
    }

    override fun perform() {
        if (CmdCopyChunk.chunk == null) {
            sender.sendMessage("No chunk copied")
            return
        }

        val time = measureNanoTime {
            val bukkitChunk = spigotPlayer.chunk
            val reader = AW.createReader(CmdCopyChunk.chunk!!)
            val ch = AW.codec.decode(reader, AW.storageChunkFactory)
            AW.chunkIO.writeChunk(bukkitChunk, ch, ChunkWriteOptions())
        }

        sender.sendMessage("Pasted chunk in ${time / 1000000}ms")
    }
}