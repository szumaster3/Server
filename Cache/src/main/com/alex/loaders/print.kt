package com.alex.loaders

import com.alex.store.Store
import java.io.File

fun print(cache: Store, outputFile: String) {
    val defs = BasDefinition.load(cache) ?: return
    val file = File(outputFile)
    file.printWriter().use { writer ->
        for (bas in defs) {
            if (!bas.isLoaded) continue
            writer.println("[bas_${bas.id}]")
            if (bas.readyAnimL != -1)
                writer.println("readyanim_l=seq_${bas.readyAnimL}")
            if (bas.readyAnimR != -1 && bas.readyAnimL != -1)
                writer.println("readyanim_r=seq_${bas.readyAnimR}")
            if (bas.walkAnimB != -1)
                writer.println("walkanim_b=seq_${bas.walkAnimB}")
            if (bas.walkAnimL != -1)
                writer.println("walkanim_l=seq_${bas.walkAnimL}")
            if (bas.walkAnimR != -1)
                writer.println("walkanim_r=seq_${bas.walkAnimR}")
            if (bas.runAnim != -1)
                writer.println("runanim=seq_${bas.runAnim}")
            if (bas.runAnimB != -1)
                writer.println("runanim_b=seq_${bas.runAnimB}")
            if (bas.runAnimL != -1)
                writer.println("runanim_l=seq_${bas.runAnimL}")
            if (bas.runAnimR != -1)
                writer.println("runanim_r=seq_${bas.runAnimR}")

            if (bas.hillRotateX != 0 || bas.hillRotateY != 0)
                writer.println("hillrotate=${bas.hillRotateX},${bas.hillRotateY}")
            if (bas.readyAnim != -1 || bas.readyAnimR != -1) {
                val ready = if (bas.readyAnim == -1) "null" else "seq_${bas.readyAnim}"
                val walk = if (bas.readyAnimR == -1) "null" else "seq_${bas.readyAnimR}"
                writer.println("readyanim=$ready,$walk")
            }

            bas.unknown27?.forEachIndexed { idx, arr ->
                if (arr != null) {
                    writer.print("unknown27=$idx")
                    arr.forEach { writer.print(",$it") }
                    writer.println()
                }
            }

            if (bas.turnSpeed != 0)
                writer.println("turnspeed=${bas.turnSpeed}")
            if (bas.turnAcceleration != 0)
                writer.println("turnacceleration=${bas.turnAcceleration}")
            if (bas.unknown31 != 0)
                writer.println("unknown31=${bas.unknown31}")
            if (bas.unknown32 != 0)
                writer.println("unknown32=${bas.unknown32}")
            if (bas.unknown34 != 0)
                writer.println("unknown34=${bas.unknown34}")
            if (bas.unknown35 != 0)
                writer.println("unknown35=${bas.unknown35}")
            if (bas.unknown36 != 0)
                writer.println("unknown36=${bas.unknown36}")
            if (bas.walkSpeed > 0)
                writer.println("walkspeed=${bas.walkSpeed}")

            writer.println()
        }
    }
}