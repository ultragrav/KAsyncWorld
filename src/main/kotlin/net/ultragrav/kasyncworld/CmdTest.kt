package net.ultragrav.kasyncworld

import net.minecraft.world.level.block.Blocks
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.world.contract.AsyncWorld
import org.bukkit.Material
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class CmdTest : SpigotCommand() {
    init {
        addAlias("test")
    }

    override fun perform() {
        val pos = spigotPlayer.location.toVector().toBlockVector()
        val timeNs = measureNanoTime {
            AW.editSync(spigotPlayer.world, AsyncWorld.EditType.DENSE) {
                for (dx in -50..50) {
                    for (dz in -50..50) {
                        for (dy in -100..100) {
                            val x = pos.blockX + dx
                            val y = pos.blockY + dy
                            val z = pos.blockZ + dz
                            setBlock(x, y, z, Blocks.AIR.defaultBlockState())
                        }
                    }
                }
            }
        }
        tell("&7Done in &a${timeNs / 1000000.0}&7ms")
    }
}