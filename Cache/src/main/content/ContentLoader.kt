package content

import com.alex.Cache
import com.alex.loaders.sprites.SpriteUtils
import com.alex.utils.Utils

object ContentLoader {

    @JvmStatic
    fun main(args: Array<String>)
    {
        runCatching {
            println("Creating backup...")
            Utils.backupCache()
            println("Backup created. Initializing cache...")

            Cache.init()
            println("Cache initialized. Populating cache...")

            load()
            println("Cache populated successfully.")
        }.onFailure { e ->
            e.printStackTrace()
        }

    }

    private fun load()
    {
        sprites()
        interfaces()
    }

    private fun sprites()
    {
        SpriteUtils.importSprites()
    }

    private fun interfaces()
    {
        content.interfaces.CustomSpellBookInterface.add()
    }
}