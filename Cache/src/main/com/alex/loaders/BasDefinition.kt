package com.alex.loaders

import com.alex.io.InputStream
import com.alex.io.OutputStream
import com.alex.store.Store
import com.alex.utils.Utils

class BasDefinition(val id: Int) {
    var isLoaded: Boolean = false
    var readyAnim: Int = -1
    var readyAnimL: Int = -1
    var readyAnimR: Int = -1
    var walkAnimB: Int = -1
    var walkAnimL: Int = -1
    var walkAnimR: Int = -1
    var runAnim: Int = -1
    var runAnimB: Int = -1
    var runAnimL: Int = -1
    var runAnimR: Int = -1
    var hillRotateX: Int = 0
    var hillRotateY: Int = 0
    var unknown27: Array<IntArray?>? = null
    var turnSpeed: Int = 0
    var turnAcceleration: Int = 0
    var unknown31: Int = 0
    var unknown32: Int = 0
    var unknown34: Int = 0
    var unknown35: Int = 0
    var unknown36: Int = 0
    var walkSpeed: Int = 0

    fun decode(stream: InputStream)
    {
        while (true) {
            val opcode = stream.readUnsignedByte()
            if (opcode == 0) break
            decodeOpcode(opcode, stream)
        }
    }

    private fun decodeOpcode(opcode: Int, stream: InputStream) {
        when (opcode) {
            1 -> {
                readyAnim = stream.readUnsignedShort()
                readyAnimR = stream.readUnsignedShort()
                if (readyAnim == 65535) readyAnim = -1
                if (readyAnimR == 65535) readyAnimR = -1
            }
            2 -> {} // crawlanim,
            3 -> {} // crawlanim_b
            4 -> {} // crawlanim_l
            5 -> {} // crawlanim_r
            6 -> runAnim = stream.readUnsignedShort()
            7 -> runAnimB = stream.readUnsignedShort()
            8 -> runAnimL = stream.readUnsignedShort()
            9 -> runAnimR = stream.readUnsignedShort()
            26 -> {
                hillRotateX = stream.readUnsignedByte()
                hillRotateY = stream.readUnsignedByte()
            }
            27 -> {
                if (unknown27 == null) unknown27 = Array(12) { null }
                val idx = stream.readUnsignedByte()
                val arr = IntArray(6) { stream.readShortSmart() }
                unknown27!![idx] = arr
            }
            29 -> turnSpeed = stream.readUnsignedByte()
            30 -> turnAcceleration = stream.readUnsignedShort()
            31 -> unknown31 = stream.readUnsignedByte()
            32 -> unknown32 = stream.readUnsignedShort()
            34 -> unknown34 = stream.readUnsignedByte()
            35 -> unknown35 = stream.readUnsignedShort()
            36 -> unknown36 = stream.readShortSmart()
            37 -> walkSpeed = stream.readUnsignedByte()
            38 -> readyAnimL = stream.readUnsignedShort()
            39 -> readyAnimR = stream.readUnsignedShort()
            40 -> walkAnimB = stream.readUnsignedShort()
            41 -> walkAnimL = stream.readUnsignedShort()
            42 -> walkAnimR = stream.readUnsignedShort()
            else -> throw RuntimeException("Unknown opcode=$opcode for=$id")
        }
    }

    fun encode(stream: OutputStream) {
        fun write(opcode: Int, block: OutputStream.() -> Unit) {
            stream.writeByte(opcode)
            stream.block()
        }
        if (readyAnim != -1 || readyAnimR != -1) write(1) {
            writeShort(if (readyAnim == -1) 65535 else readyAnim)
            writeShort(if (readyAnimR == -1) 65535 else readyAnimR)
        }
        if (runAnim != -1) write(6) { writeShort(runAnim) }
        if (runAnimB != -1) write(7) { writeShort(runAnimB) }
        if (runAnimL != -1) write(8) { writeShort(runAnimL) }
        if (runAnimR != -1) write(9) { writeShort(runAnimR) }
        if (hillRotateX != 0 || hillRotateY != 0) write(26) {
            writeByte(hillRotateX / 4)
            writeByte(hillRotateY / 4)
        }
        unknown27?.forEachIndexed { idx, arr ->
            if (arr != null) write(27) {
                writeByte(idx)
                arr.forEach { writeShortSmart(it) }
            }
        }
        if (turnSpeed != 0) write(29) { writeByte(turnSpeed) }
        if (turnAcceleration != 0) write(30) { writeShort(turnAcceleration) }
        if (unknown31 != 0) write(31) { writeByte(unknown31) }
        if (unknown32 != 0) write(32) { writeShort(unknown32) }
        if (unknown34 != 0) write(34) { writeByte(unknown34) }
        if (unknown35 != 0) write(35) { writeShort(unknown35) }
        if (unknown36 != 0) write(36) { writeShortSmart(unknown36) }
        if (walkSpeed != 0) write(37) { writeByte(walkSpeed) }

        if (readyAnimL != -1) write(38) { writeShort(readyAnimL) }
        if (readyAnimR != -1) write(39) { writeShort(readyAnimR) }

        if (walkAnimB != -1) write(40) { writeShort(walkAnimB) }
        if (walkAnimL != -1) write(41) { writeShort(walkAnimL) }
        if (walkAnimR != -1) write(42) { writeShort(walkAnimR) }

        stream.writeByte(0)
    }

    companion object {
        fun load(cache: Store): Array<BasDefinition>? {
            return try {
                val index = cache.indexes[2]
                val size = Utils.getRenderAnimationDefinitionsSize(cache)
                val definitions = Array(size) { BasDefinition(it) }
                for (id in definitions.indices) {
                    val data = index.getFile(32, id)
                    if (data != null) {
                        definitions[id].decode(InputStream(data))
                        definitions[id].isLoaded = true
                    }
                }
                definitions
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
    }
}