package net.ultragrav.kasyncworld.cmd

import net.kyori.adventure.text.Component
import net.minecraft.world.level.block.Blocks
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.editSync
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import kotlin.system.measureNanoTime

class CmdTest : SpigotCommand() {
    init {
        addAlias("test")
    }

    override fun perform() {
        val pos = spigotPlayer.location.toVector().toBlockVector()
        val pl = spigotPlayer
        val timeNanos = System.nanoTime()
        spigotPlayer.world.editSync(AsyncWorld.EditType.SPARSE) {
            val writeTime = measureNanoTime {
                for (dx in -1..1) {
                    for (dz in -1..1) {
                        for (dy in -1 downTo -1) {
                            val x = pos.blockX + dx
                            val y = pos.blockY + dy
                            val z = pos.blockZ + dz
                            setBlock(x, y, z, Blocks.CHEST.defaultBlockState())
                        }
                    }
                }
            }
            pl.sendMessage(Component.text("Queued in ${writeTime / 1000000.0}ms"))
        }
//            .thenAccept {
            val time = (System.nanoTime() - timeNanos) / 1000000.0
            pl.sendMessage(Component.text("Done in $time ms"))
//        }
    }
}