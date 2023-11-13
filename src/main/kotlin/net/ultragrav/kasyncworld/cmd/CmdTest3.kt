package net.ultragrav.kasyncworld.cmd

import net.jpountz.lz4.LZ4Factory
import net.ultragrav.command.platform.SpigotCommand
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.world.chunk.codec.LZ4WrappingCodec
import net.ultragrav.kasyncworld.world.chunk.io.ChunkReadOptions
import net.ultragrav.kasyncworld.world.inmemory.pack.PackedWorld
import org.bukkit.Tag
import kotlin.system.measureTimeMillis

class CmdTest3 : SpigotCommand() {
    init {
        addAlias("test3")
    }

    override fun perform() {
        val world = CmdTest.currWorld!!

        val pack: PackedWorld
        val time = measureTimeMillis {
            pack = world.saveAndPack()
        }

        val packBytes: ByteArray
        val packSerializationMs = measureTimeMillis {
            packBytes = AW.serializePackedWorld(pack)
        }

        spigotPlayer.sendMessage("Saved and packed in $time ms")
        spigotPlayer.sendMessage("Serialized in $packSerializationMs ms")
        spigotPlayer.sendMessage("Size: ${packBytes.size} bytes")

        val compressionMs = measureTimeMillis {
            val compressed = LZ4Factory.fastestJavaInstance().fastCompressor()
                .compress(packBytes)
            spigotPlayer.sendMessage("Compressed size: ${compressed.size} bytes")
        }
        spigotPlayer.sendMessage("Compressed in $compressionMs ms")

    }
}