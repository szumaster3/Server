package content.region.kandarin.port_khazard.plugin

import content.data.items.RustyItem
import content.region.kandarin.port_khazard.dialogue.TindelMerchantDialogue
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.Location
import core.tools.RandomFunction
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Sounds

/**
 * Handles the exchange of rusty weapons.
 */
class TindelMerchantPlugin : InteractionListener {

    companion object {
        private const val TINDEL = NPCs.TINDEL_MARCHANT_1799
        private const val SWORD = Items.RUSTY_SWORD_686
        private const val SCIMITAR = Items.RUSTY_SCIMITAR_6721

        private const val STALL = shared.consts.Scenery.ANTIQUES_SHOP_STALL_5831
        private const val BELL_SFX = Sounds.BELL_2192

        private const val COINS = Items.COINS_995
        private const val COINS_REQUIRED = 100

        @JvmStatic
        fun success(player: Player, skill: Int): Boolean {
            val level = player.getSkills().getLevel(skill).toDouble()
            val minChance = 50.0
            val maxChance = 100.0
            val maxLevel = 99.0

            val successChance = minChance + (level - 1) * (maxChance - minChance) / (maxLevel - 1)
            val roll = RandomFunction.random(0.0, 100.0)
            return roll < successChance
        }

        fun exchangeRustyWeapon(player: Player) {
            val weaponToType = mapOf(
                SWORD to RustyItem.EquipmentType.SWORDS,
                SCIMITAR to RustyItem.EquipmentType.SCIMITARS
            )

            val weaponEntry = weaponToType.entries.firstOrNull { player.inventory.contains(it.key, 1) }
                ?: run {
                    sendNPCDialogue(player, TINDEL, "Sorry my friend, but you don't seem to have any swords that need to be identified.", FaceAnim.HALF_GUILTY)
                    return
                }

            val weaponId = weaponEntry.key
            val equipType = weaponEntry.value

            if (!player.inventory.contains(COINS, COINS_REQUIRED)) {
                sendNPCDialogue(player, TINDEL, "Sorry, you don't have enough coins.", FaceAnim.HALF_GUILTY)
                return
            }

            sendDoubleItemDialogue(player, weaponId, Items.COINS_8896, "You hand Tindel 100 coins plus the ${getItemName(weaponId).lowercase()}.")

            addDialogueAction(player) { _, _ ->
                val repaired = RustyItem.getRepair(equipType)
                    ?: return@addDialogueAction

                if (!removeItem(player, Item(COINS, COINS_REQUIRED)) ||
                    !removeItem(player, Item(weaponId))
                ) {
                    return@addDialogueAction
                }

                if (success(player, Skills.SMITHING)) {
                    sendItemDialogue(player, repaired.id, "Tindel gives you a ${getItemName(repaired.id).lowercase()}.")
                    addItem(player, repaired.id)
                } else {
                    sendNPCDialogue(player, TINDEL, "Sorry my friend, but the item wasn't worth anything. I've disposed of it for you.", FaceAnim.HALF_GUILTY)
                }
            }
        }
    }

    override fun defineListeners() {

        /*
         * Handles ring the bell at Port Khazard.
         */

        on(STALL, IntType.SCENERY, "ring-bell") { player, _ ->
            playGlobalAudio(player.location, BELL_SFX)
            sendDialogue(player, "You ring for attention.")
            runTask(player, 3) {
                faceLocation(findLocalNPC(player, TINDEL) ?: return@runTask, player.location)
                openDialogue(player, TindelMerchantDialogue())
            }
            return@on true
        }

        /*
         * Handles Tindel Merchant options.
         */

        on(TINDEL, IntType.NPC, "talk-to", "Give-Sword") { player, _ ->
            when (getUsedOption(player)) {
                "talk-to" -> openDialogue(player, TindelMerchantDialogue())
                "give-sword" -> exchangeRustyWeapon(player)
                else -> sendMessage(player, "You can't reach!")
            }
            return@on true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.NPC, intArrayOf(TINDEL), "talk-to", "give-sword") { _, _ ->
            return@setDest Location(2678, 3152, 0)
        }
    }
}
