package net.ultragrav.kasyncworld.world.chunk.codec

import net.jpountz.lz4.LZ4Compressor
import net.jpountz.lz4.LZ4Factory
import net.ultragrav.kasyncworld.AW
import net.ultragrav.kasyncworld.data.DataReader
import net.ultragrav.kasyncworld.data.DataWriter
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunk
import net.ultragrav.kasyncworld.world.chunk.contract.AsyncChunkFactory

class LZ4WrappingCodec(private val child: ChunkCodec) : ChunkCodec {
    override val id: String
        get() = "${child.id}+lz4"
    override val version: Int
        get() = child.version

    override fun earlierVersion(): ChunkCodec? {
        return LZ4WrappingCodec(child.earlierVersion() ?: return null)
    }

    override fun encode(writer: DataWriter, chunk: AsyncChunk) {
        val childWriter = AW.createWriter()
        child.encode(childWriter, chunk)
        val bytes = childWriter.toByteArray()
        val compressor = factory.fastCompressor()
        val compressed = compressor.compress(bytes)
        writer.writeByteArray(compressed)
    }

    override fun decode(reader: DataReader, factory: AsyncChunkFactory): AsyncChunk {
        val bytes = reader.readByteArray()
        val decompressor = LZ4WrappingCodec.factory.fastDecompressor()
        val decompressed = decompressor.decompress(bytes, bytes.size * 10)
        val childReader = AW.createReader(decompressed)
        return child.decode(childReader, factory)
    }

    companion object {
        private val factory = LZ4Factory.fastestJavaInstance()
    }
}