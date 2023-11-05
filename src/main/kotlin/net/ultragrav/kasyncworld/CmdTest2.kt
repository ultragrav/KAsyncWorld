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
                for (dx in -10..10) {
                    for (dz in -10..10) {
                        val x = dx + pos.blockX
                        val z = dz + pos.blockZ
                        val y = pos.blockY - 1
                        setBlock(x, y, z, Blocks.STONE.defaultBlockState())
                    }
                }
            }
        }
        tell("&7Done in &a${timeNs / 1000000.0}&7ms")
    }
}