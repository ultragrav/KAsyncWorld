package net.ultragrav.kasyncworld

import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
    override fun onEnable() {
        AW.initialize(this)
        CmdTest().register()
        CmdTest2().register()
    }
}