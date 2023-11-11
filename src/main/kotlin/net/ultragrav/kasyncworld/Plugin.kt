package net.ultragrav.kasyncworld

import net.ultragrav.kasyncworld.cmd.CmdCopyChunk
import net.ultragrav.kasyncworld.cmd.CmdPasteChunk
import net.ultragrav.kasyncworld.cmd.CmdTest
import net.ultragrav.kasyncworld.cmd.CmdTest2
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
    override fun onEnable() {
        AW.initialize(this)
        CmdTest().register()
        CmdTest2().register()
        CmdCopyChunk().register()
        CmdPasteChunk().register()
    }
}