package content.global.skill.firemaking

import content.data.items.DyeItem
import core.api.*
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.impl.Projectile
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.RegionManager.getObject
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Quests
import java.util.*
import kotlin.math.min

class OrigamiBalloonPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles creating the balloon structure.
         */

        onUseWith(IntType.ITEM, Items.PAPYRUS_970, Items.BALL_OF_WOOL_1759) { player, used, wool ->
            if (getQuestStage(player, Quests.ENLIGHTENED_JOURNEY) < 1) {
                sendMessage(player, "You need to start the ${Quests.ENLIGHTENED_JOURNEY} quest in order to make this.")
                return@onUseWith false
            }

            if (removeItem(player, used.asItem()) && removeItem(player, wool.asItem())) {
                sendMessage(player, "You create the origami balloon structure.")
                animate(player, Animations.CRAFT_BONGOS_5140)
                addItemOrDrop(player, Items.BALLOON_STRUCTURE_9933)
            }
            return@onUseWith true
        }

        /*
         * Handles creating origami balloon.
         */

        onUseWith(IntType.ITEM, intArrayOf(Items.CANDLE_36, Items.BLACK_CANDLE_38), Items.BALLOON_STRUCTURE_9933) { player, used, with ->
            if (removeItem(player, Item(used.id)) && removeItem(player, Item(with.id))) {
                sendMessage(player, "You create the origami balloon.")
                rewardXP(player, Skills.CRAFTING, 35.0)
                animate(player, Animations.CRAFT_BONGOS_5140)
                addItemOrDrop(player, Items.ORIGAMI_BALLOON_9934)
            }
            return@onUseWith true
        }

        /*
         * Handles dyeing balloons.
         */

        onUseWith(IntType.ITEM, DYE_IDS, Items.ORIGAMI_BALLOON_9934) { player, used, balloon ->

            val dye = DyeItem.forId(used.id) ?: return@onUseWith true
            val maxAmount = min(amountInInventory(player, dye.dyeId), amountInInventory(player, balloon.id))

            if (maxAmount == 0) return@onUseWith true

            sendSkillDialogue(player) {
                withItems(dye.origamiBallonId)

                create { _, amount ->
                    runTask(player, 2, amount) {
                        if (!inInventory(player, dye.dyeId) || !inInventory(player, Items.ORIGAMI_BALLOON_9934)) return@runTask
                        removeItem(player, Item(dye.dyeId))
                        removeItem(player, Item(Items.ORIGAMI_BALLOON_9934))
                        addItem(player, dye.origamiBallonId, 1)
                    }
                }

                calculateMaxAmount { maxAmount }
            }

            return@onUseWith true
        }

        /*
         * Lighting and releasing balloons.
         */

        onUseWith(IntType.ITEM, Items.TINDERBOX_590, *BALLOON_IDS) { player, _, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true
            if (getStatLevel(player, Skills.FIREMAKING) < 20) {
                sendMessage(player, "You need a Firemaking level of 20 to light the balloon.")
                return@onUseWith true
            }
            if (getObject(player.location) != null || player.zoneMonitor.isInZone("bank")) {
                sendMessage(player, "You can't light a balloon here.")
                return@onUseWith true
            }
            if (!removeItem(player, with.asItem())) return@onUseWith true

            val flyUpwardsGfx = 880
            val projectileGfx = 880 + 2

            queueScript(player, 1, QueueStrength.WEAK) {
                visualize(player, Animations.BALLOON_FLY_5142, flyUpwardsGfx)
                sendMessage(player, "You light the origami ${getItemName(with.id).lowercase(Locale.getDefault())}.")
                delayClock(player, Clocks.SKILLING, 3)
                rewardXP(player, Skills.FIREMAKING, 20.0)
                Projectile
                    .create(player, null, projectileGfx, 45, 45, 1, 70, 0)
                    .transform(player, player.location.transform(player.direction, (player.direction.ordinal + 1) % 8), false, 70, 140)
                    .send()
                stopExecuting(player)
            }

            return@onUseWith true
        }
    }

    companion object {
        /**
         * The balloon graphics.
         */


        /**
         * The dyes.
         */
        private val DYE_IDS = DyeItem.values().map { it.dyeId }.toIntArray()

        /**
         * The coloured origami balloons.
         */
        private val BALLOON_IDS = (DyeItem.values().map { it.origamiBallonId } + Items.ORIGAMI_BALLOON_9934).toIntArray()
    }
}
