package net.ultragrav.kasyncworld

import net.ultragrav.kasyncworld.cmd.*
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
    override fun onEnable() {
        AW.initialize(this)
        CmdTest().register()
        CmdTest2().register()
        CmdTest3().register()
        CmdCopyChunk().register()
        CmdPasteChunk().register()
    }
}