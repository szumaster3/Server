package content

import com.alex.Cache
import com.alex.store.Index
import java.io.File
import java.nio.file.Files

object ModelLoader {

    private val modelIndex: Index
        get() = Cache.getStore()!!.indexes[7]

    @JvmStatic
    fun importModels() {
        val folder = File("../Assets/models/")

        if (!folder.exists() || !folder.isDirectory) return
        folder.walk().forEach { file ->
            if (!file.isFile || file.extension.lowercase() != "dat") return@forEach
            try {
                val id = file.nameWithoutExtension.toIntOrNull() ?: return@forEach
                val data = Files.readAllBytes(file.toPath())
                modelIndex.putFile(id, 0, data)
            } catch (_: Exception) {

            }
        }
    }
}