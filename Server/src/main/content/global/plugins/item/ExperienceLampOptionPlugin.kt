package content.global.plugins.item

import content.data.items.LampItem
import core.api.*
import core.game.component.Component
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.tools.BLUE
import shared.consts.Components
import shared.consts.Items

class ExperienceLampOptionPlugin : InteractionListener {
    private val xpGainItems = LampItem.values().map { it.item }.toIntArray()

    override fun defineListeners() {
        /*
         * Handles the "rub" and "read" interactions for experience lamps.
         */

        on(xpGainItems, IntType.ITEM, "rub", "read") { player, node ->
            setAttribute(player, "caller") { skill: Int, _: Player ->
                player.lock()
                setAttribute(player, "xp_reward_item", node)
                val lamp = LampItem.forItem(player.getAttribute("xp_reward_item", Item(Items.LAMP_2528)))
                if (lamp == null) {
                    sendMessage(player, "This lamp cannot be used.")
                    return@setAttribute
                }

                if (getStatLevel(player, skill) < lamp.requiredLevel) {
                    sendMessage(player, "You need at least ${lamp.requiredLevel} ${Skills.SKILL_NAME[skill]} to use this lamp.")
                    return@setAttribute
                }

                val itemToRemove = player.getAttribute<Any>("xp_reward_item") as? Item
                if (itemToRemove != null && removeItem(player, itemToRemove)) {
                    val xp = if (lamp == LampItem.RANDOM_EVENT_LAMP_0) getStatLevel(player, skill) * 10 else lamp.experience
                    rewardXP(player, skill, xp.toDouble())

                    val messageTitle = if (lamp.item in intArrayOf(
                            Items.TOME_OF_XP_3_9656,
                            Items.TOME_OF_XP_2_9657,
                            Items.TOME_OF_XP_1_9658,
                            Items.TOME_OF_XP_2ND_ED_3_13160,
                            Items.TOME_OF_XP_2ND_ED_2_13161,
                            Items.TOME_OF_XP_2ND_ED_1_13162
                        )) {
                        "You read a fascinating chapter and earn experience!"
                    } else {
                        "Your wish has been granted!"
                    }

                    sendPlainDialogue(player,
                        false,
                        BLUE + messageTitle,
                        "You have been awarded $xp ${Skills.SKILL_NAME[skill]} experience!"
                    )
                }
            }

            player.interfaceManager.open(
                Component(Components.STATS_ADVANCEMENT_134).setUncloseEvent { _, _ ->
                    player.interfaceManager.openDefaultTabs()
                    removeAttribute(player, "xp_reward_item")
                    player.unlock()
                    return@setUncloseEvent true
                }
            )

            removeTabs(player, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13)
            return@on true
        }
    }
}
