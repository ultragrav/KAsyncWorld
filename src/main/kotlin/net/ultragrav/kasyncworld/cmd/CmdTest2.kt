package net.ultragrav.kasyncworld.cmd

import net.minecraft.world.level.block.Blocks
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.getSectionIndexMB
import net.ultragrav.kasyncworld.world.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.contract.AsyncWorld

class CmdTest2 : SpigotCommand() {
    init {
        addAlias("test2")
    }

    override fun perform() {
        val pos = spigotPlayer.location.toVector().toBlockVector()
        val sender = sender
        val nanoTime = System.nanoTime()
        AW.editAsync(spigotPlayer.world, AsyncWorld.EditType.SPARSE) {
            val cx = pos.blockX shr 4
            val cz = pos.blockZ shr 4
            for (dx in -8..8) {
                for (dz in -8..8) {
                    val chunkX = cx + dx
                    val chunkZ = cz + dz
                    val chunk = getChunk(chunkX, chunkZ)
                    clearChunk(chunk)
                }
            }
        }.thenAccept {
            val time = (System.nanoTime() - nanoTime) / 1000000.0
            sender.sendMessage("Done in $time ms")
        }
    }

    fun clearChunk(chunk: AsyncChunk) {
        chunk.sections.map {
            chunk.createSection()
        }.forEachIndexed { index, it ->
            it.blocks.iterationStrategy.setAll()
            chunk.setSection(chunk.heightOptions.getSectionIndexMB(index), it)
        }
    }
}