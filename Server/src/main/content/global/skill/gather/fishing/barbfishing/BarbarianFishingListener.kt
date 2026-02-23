package content.global.skill.gather.fishing.barbfishing

import content.region.kandarin.baxtorian.BarbarianTraining
import core.api.*
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs

class BarbarianFishingListener : InteractionListener {

    override fun defineListeners() {
        defineInteraction(
            IntType.NPC,
            intArrayOf(NPCs.FISHING_SPOT_1176),
            "Fish",
            persistent = true,
            allowedDistance = 1,
            handler = ::handleFishing
        )
    }

    private fun handleFishing(player: Player, node: Node, state: Int): Boolean {
        val npc = node as? NPC ?: return clearScripts(player)

        if (!finishedMoving(player))
            return restartScript(player)

        if (state == 0) {
            if (!checkRequirements(player, npc))
                return clearScripts(player)

            sendMessage(player, "You cast out your line...")
        }

        if (!clockReady(player, Clocks.SKILLING))
            return keepRunning(player)

        playAnimation(player)

        val bait = findBait(player)
        if (bait == null) {
            sendMessage(player, "You run out of bait.")
            return clearScripts(player)
        }

        val fish = rollFish(player)

        if (!hasSpaceFor(player, fish)) {
            sendMessage(player, "You don't have enough space in your inventory.")
            return clearScripts(player)
        }

        if (rollSuccess(player, fish.id)) {
            if (!removeItem(player, bait)) {
                sendMessage(player, "You run out of bait.")
                return clearScripts(player)
            }

            giveReward(player, fish)
        }

        delayClock(player, Clocks.SKILLING, 5)

        if (!checkRequirements(player, npc))
            return clearScripts(player)

        return keepRunning(player)
    }

    private fun playAnimation(player: Player) {
        player.animate(Animation(Animations.ROD_FISHING_622))
    }

    private fun giveReward(player: Player, fish: Item) {
        addItem(player, fish.id)

        rewardXP(player, Skills.FISHING, fishingXP(fish.id))
        rewardXP(player, Skills.AGILITY, sharedXP(fish.id))
        rewardXP(player, Skills.STRENGTH, sharedXP(fish.id))

        sendMessage(player, "You catch a ${fish.name.lowercase()}.")

        if (!getAttribute(player, BarbarianTraining.FISHING_BASE, false)) {
            sendDialogueLines(
                player,
                "You feel you have learned more of barbarian ways.",
                "Otto might wish to talk to you more."
            )
            setAttribute(player, BarbarianTraining.FISHING_BASE, true)
            player.savedData.activityData.isBarbarianFishingRod = true
        }
    }

    private fun checkRequirements(player: Player, node: Node): Boolean {
        val fishing = getStatLevel(player, Skills.FISHING)
        val agility = getStatLevel(player, Skills.AGILITY)
        val strength = getStatLevel(player, Skills.STRENGTH)

        if (fishing < 48) {
            sendMessage(player, "You need a Fishing level of at least 48 to fish here.")
            return false
        }

        if (agility < 15 || strength < 15) {
            val stat = when {
                agility < 15 && strength < 15 -> "agility and strength"
                agility < 15 -> "agility"
                else -> "strength"
            }
            sendMessage(player, "You need a $stat level of at least 15 to fish here.")
            return false
        }

        if (!getAttribute(player, BarbarianTraining.FISHING_START, false)) {
            sendDialogue(
                player,
                "You must begin the relevant section of Otto Godblessed's barbarian training."
            )
            return false
        }

        if (findBait(player) == null) {
            sendMessage(player, "You don't have any bait to fish with.")
            return false
        }

        if (player.inventory.isFull) {
            sendMessage(player, "You don't have enough space in your inventory.")
            return false
        }

        return node.isActive && node.location.withinDistance(player.location, 1)
    }

    private fun rollFish(player: Player): Item {
        val available = mutableListOf(Items.LEAPING_TROUT_11328)

        if (player.skills.getLevel(Skills.FISHING) >= 58 &&
            player.skills.getLevel(Skills.STRENGTH) >= 30 &&
            player.skills.getLevel(Skills.AGILITY) >= 30
        ) {
            available += Items.LEAPING_SALMON_11330
        }

        if (player.skills.getLevel(Skills.FISHING) >= 70 &&
            player.skills.getLevel(Skills.STRENGTH) >= 45 &&
            player.skills.getLevel(Skills.AGILITY) >= 45
        ) {
            available += Items.LEAPING_STURGEON_11332
        }

        return Item(available.random())
    }

    private fun rollSuccess(player: Player, fishId: Int): Boolean {
        val level = player.skills.getLevel(Skills.FISHING) +
                player.familiarManager.getBoost(Skills.FISHING)

        val difficulty = when (fishId) {
            Items.LEAPING_TROUT_11328 -> 48
            Items.LEAPING_SALMON_11330 -> 58
            Items.LEAPING_STURGEON_11332 -> 70
            else -> 1
        }

        val chance = level.toDouble() / (difficulty + 10)
        return Math.random() < chance.coerceIn(0.05, 0.95)
    }

    private fun fishingXP(fishId: Int) = when (fishId) {
        Items.LEAPING_TROUT_11328 -> 50.0
        Items.LEAPING_SALMON_11330 -> 70.0
        Items.LEAPING_STURGEON_11332 -> 80.0
        else -> 0.0
    }

    private fun sharedXP(fishId: Int) = when (fishId) {
        Items.LEAPING_TROUT_11328 -> 5.0
        Items.LEAPING_SALMON_11330 -> 6.0
        Items.LEAPING_STURGEON_11332 -> 7.0
        else -> 0.0
    }

    private val baitItems = listOf(
        Items.FISHING_BAIT_313,
        Items.FEATHER_314,
        Items.STRIPY_FEATHER_10087,
        Items.RED_FEATHER_10088,
        Items.BLUE_FEATHER_10089,
        Items.YELLOW_FEATHER_10090,
        Items.ORANGE_FEATHER_10091,
        Items.FISH_OFFCUTS_11334,
        Items.ROE_11324,
        Items.CAVIAR_11326
    )

    private fun findBait(player: Player): Int? =
        baitItems.firstOrNull { inInventory(player, it) }
}
