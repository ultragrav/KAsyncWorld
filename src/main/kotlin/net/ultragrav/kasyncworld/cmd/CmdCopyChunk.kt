package net.ultragrav.kasyncworld.cmd

import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import net.ultragrav.kasyncworld.world.versionio.ChunkReadOptions
import kotlin.system.measureNanoTime

class CmdCopyChunk : SpigotCommand() {

    init {
        addAlias("copychunk")
    }

    override fun perform() {
        val time = measureNanoTime {
            val bukkitChunk = spigotPlayer.chunk
            val ch = AW.chunkIO.readChunk(bukkitChunk, AW.createAsyncWorld(spigotPlayer.world, AsyncWorld.EditType.SPARSE), ChunkReadOptions())
            val writer = AW.createWriter()
            AW.codec.encode(writer, ch)
            chunk = writer.toByteArray()
        }
        sender.sendMessage("Copied chunk in ${time / 1000000}ms")
    }

    companion object {
        var chunk: ByteArray? = null
    }
}