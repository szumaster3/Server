package content.region.kandarin.west_ardougne.diary.plugin

import com.google.gson.JsonObject
import content.data.GameAttributes
import core.ServerStore.Companion.getArchive
import core.ServerStore.Companion.getBoolean
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.link.TeleportManager
import core.game.world.map.Location
import shared.consts.Items

class ArdougneCloakPlugin : InteractionListener {

    private val ARDOUGNE_CLOAK_IDS = intArrayOf(
        Items.ARDOUGNE_CLOAK_1_14701,
        Items.ARDOUGNE_CLOAK_2_14702,
        Items.ARDOUGNE_CLOAK_3_14703
    )

    override fun defineListeners() {

        /*
         * Handles teleport option for diary cloaks.
         */

        on(ARDOUGNE_CLOAK_IDS, IntType.ITEM, "teleports", "operate") { player, node ->
            when (node.id) {
                ARDOUGNE_CLOAK_IDS[0] -> if (hasTimerActive(player, GameAttributes.TELEBLOCK_TIMER)) {
                    sendMessage(player, "A magical force has stopped you from teleporting.")
                } else {
                    teleport(player, MONASTERY_LOCATION, TeleportManager.TeleportType.RANDOM_EVENT_OLD)
                }
                ARDOUGNE_CLOAK_IDS[1],
                ARDOUGNE_CLOAK_IDS[2] -> openDialogue(player, ArdougneCloakTeleport())
            }
            return@on true
        }
    }

    class ArdougneCloakTeleport : DialogueFile() {
        override fun handle(componentID: Int, buttonID: Int) {
            val player = player ?: return

            when (stage) {
                0 -> {
                    setTitle(player, 2)
                    sendOptions(player, "Where would you like to teleport to?", "Kandarin Monastery.", "Ardougne Farm.")
                    stage++
                }

                1 -> {
                    end()
                    when (buttonID) {
                        1 -> if (hasTimerActive(player, GameAttributes.TELEBLOCK_TIMER)) {
                            sendMessage(player, "A magical force has stopped you from teleporting.")
                        } else {
                            teleport(player, MONASTERY_LOCATION, TeleportManager.TeleportType.RANDOM_EVENT_OLD)
                        }
                        2 -> {
                            val store = getStoreFile()
                            val key = player.username.lowercase()
                            if (store.getBoolean(key)) {
                                sendDialogue(player, "This can only be done once per day.")
                            } else {
                                if (hasTimerActive(player, GameAttributes.TELEBLOCK_TIMER)) {
                                    sendMessage(player, "A magical force has stopped you from teleporting.")
                                } else {
                                    teleport(player, FARM_LOCATION, TeleportManager.TeleportType.RANDOM_EVENT_OLD)
                                    store.addProperty(key, true)
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    companion object {
        @JvmStatic
        private fun getStoreFile(): JsonObject = getArchive("daily-ardougne-farm-teleport")

        private val MONASTERY_LOCATION = Location.getRandomLocation(Location.create(2606, 3217, 0), 1, true)
        private val FARM_LOCATION = Location.getRandomLocation(Location.create(2677, 3375, 0), 1, true)
    }
}