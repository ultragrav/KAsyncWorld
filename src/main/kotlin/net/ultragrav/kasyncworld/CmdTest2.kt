package net.ultragrav.kasyncworld

import net.minecraft.world.level.block.Blocks
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import org.bukkit.Material
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class CmdTest2 : SpigotCommand() {
    init {
        addAlias("test2")
    }

    override fun perform() {
        val pos = spigotPlayer.location.toVector().toBlockVector()
        val timeNs = measureNanoTime {
            AW.editSync(spigotPlayer.world, AsyncWorld.EditType.SPARSE) {
                for (i in 0 until 100000) {
                    val x = pos.blockX + (Math.random() * 100).toInt() - 50
                    val y = pos.blockY + (Math.random() * 100).toInt() - 50
                    val z = pos.blockZ + (Math.random() * 100).toInt() - 50
                    setBlock(x, y, z, Blocks.AIR.defaultBlockState())
                }
            }
        }
        tell("&7Done in &a${timeNs / 1000000.0}&7ms")
    }
}