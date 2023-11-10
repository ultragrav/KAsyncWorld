package net.ultragrav.kasyncworld.data

import net.ultragrav.serializer.GravSerializer

class GravSerializerRead(private val ser: GravSerializer) : DataReader {
    override fun readInt(): Int {
        return ser.readInt()
    }

    override fun readByte(): Byte {
        return ser.readByte()
    }

    override fun readShort(): Short {
        return ser.readShort()
    }

    override fun readLong(): Long {
        return ser.readLong()
    }

    override fun readFloat(): Float {
        return ser.readFloat()
    }

    override fun readDouble(): Double {
        return ser.readDouble()
    }

    override fun readBoolean(): Boolean {
        return ser.readBoolean()
    }

    override fun readString(): String {
        return ser.readString()
    }

    override fun readByteArray(): ByteArray {
        return ser.readByteArray()
    }

    override fun readChar(): Char {
        return ser.readChar()
    }

    override fun readIntArray(): IntArray {
        val length = readInt()
        val arr = IntArray(length)
        for (i in 0 until length) {
            arr[i] = readInt()
        }
        return arr
    }

    override fun readLongArray(): LongArray {
        val length = readInt()
        val arr = LongArray(length)
        for (i in 0 until length) {
            arr[i] = readLong()
        }
        return arr
    }
}