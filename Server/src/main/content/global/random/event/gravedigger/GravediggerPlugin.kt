package content.global.random.event.gravedigger

import content.data.GameAttributes
import content.data.RandomEvent
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.map.zone.ZoneBorders
import core.game.world.update.flag.context.Animation
import shared.consts.*

/**
 * Handles interactions for Gravedigger random event.
 */
class GravediggerPlugin : InteractionListener {

    /**
     * Represents each coffin content for random event.
     */
    enum class CoffinSet(val displayName: String, val coffinId: Int, val gravestoneId: Int, val graveId: Int, val emptyGraveId: Int, val item: Int, val content: List<Int>) {
        LUMBERJACK(
            "Lumberjack", Items.COFFIN_7587, Scenery.GRAVESTONE_12716,
            Scenery.GRAVE_12721, Scenery.GRAVE_12726, Items.ITEM_7614,
            listOf(Items.ITEM_7611, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7603, Items.ITEM_7598, Items.ITEM_7605, Items.ITEM_7612)
        ),
        COOKS(
            "Cooks", Items.COFFIN_7588, Scenery.GRAVESTONE_12717,
            Scenery.GRAVE_12722, Scenery.GRAVE_12727, Items.ITEM_7615,
            listOf(Items.ITEM_7604, Items.ITEM_7601, Items.ITEM_7598, Items.ITEM_7600, Items.ITEM_7598, Items.ITEM_7611, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598)
        ),
        MINER(
            "Miner", Items.COFFIN_7589, Scenery.GRAVESTONE_12718,
            Scenery.GRAVE_12723, Scenery.GRAVE_12728, Items.ITEM_7616,
            listOf(Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7606, Items.ITEM_7598, Items.ITEM_7597, Items.ITEM_7598, Items.ITEM_7607, Items.ITEM_7598, Items.ITEM_7611)
        ),
        FARMER(
            "Farmer", Items.COFFIN_7590, Scenery.GRAVESTONE_12719,
            Scenery.GRAVE_12724, Scenery.GRAVE_12729, Items.ITEM_7617,
            listOf(Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7610, Items.ITEM_7611, Items.ITEM_7609, Items.ITEM_7598, Items.ITEM_7602, Items.ITEM_7598, Items.ITEM_7598)
        ),
        POTTER(
            "Potter", Items.COFFIN_7591, Scenery.GRAVESTONE_12720,
            Scenery.GRAVE_12725, Scenery.GRAVE_12730, Items.ITEM_7618,
            listOf(Items.ITEM_7598, Items.ITEM_7599, Items.ITEM_7608, Items.ITEM_7613, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7598, Items.ITEM_7611, Items.ITEM_7598)
        )
    }

    companion object {
        private const val COFFIN_INTERFACE = Components.GRAVEDIGGER_COFFIN_141
        private const val GRAVESTONE_INTERFACE = Components.GRAVEDIGGER_GRAVE_143
        private const val MAUSOLEUM = Scenery.MAUSOLEUM_12731
        private val ANIMATION = Animation(Animations.HUMAN_BURYING_BONES_827)
        private val EVENT_ZONE = ZoneBorders(1921, 4993, 1934, 5006)
        private val COFFIN_SETS = CoffinSet.values()
        private val COFFIN_MAP = COFFIN_SETS.associateBy { it.coffinId }
        private val GRAVESTONE_MAP = COFFIN_SETS.associateBy { it.gravestoneId }
        private val GRAVE_MAP = COFFIN_SETS.associateBy { it.graveId }
        private val EMPTY_GRAVE_MAP = COFFIN_SETS.associateBy { it.emptyGraveId }

        val COFFIN_IDS = COFFIN_SETS.map(CoffinSet::coffinId).toIntArray()
        private val GRAVESTONE_IDS = COFFIN_SETS.map(CoffinSet::gravestoneId).toIntArray()
        private val GRAVE_IDS = COFFIN_SETS.map(CoffinSet::graveId).toIntArray()
        private val EMPTY_GRAVE_IDS = COFFIN_SETS.map(CoffinSet::emptyGraveId).toIntArray()

        private val WOODCUTTING_TOOLS = intArrayOf(
            Items.INFERNO_ADZE_13661, Items.DRAGON_AXE_6739, Items.RUNE_AXE_1359, Items.ADAMANT_AXE_1357,
            Items.MITHRIL_AXE_1355, Items.BLACK_AXE_1361, Items.STEEL_AXE_1353, Items.IRON_AXE_1349, Items.BRONZE_AXE_1351
        )

        fun reset(player: Player) {
            COFFIN_SETS.forEach {
                removeAttributes(player, "coffin_set:${it.name}")
            }
        }

        fun cleanup(player: Player) {
            player.properties.teleportLocation = getAttribute(player, RandomEvent.save(), null)
            setMinimapState(player, 0)
            removeAttributes(player, GameAttributes.GRAVEDIGGER_SCORE, RandomEvent.save(), RandomEvent.logout())
            clearLogoutListener(player, RandomEvent.logout())
            reset(player)
        }

        fun init(player: Player) {
            COFFIN_SETS.forEach {
                player.setAttribute("coffin_set:${it.name}", it.name)
            }
        }
    }

    override fun defineListeners() {

        /*
         * Handles inspect coffin.
         */

        on(COFFIN_IDS, IntType.ITEM, "check") { player, item ->
            COFFIN_MAP[item.id]?.let { set ->
                openInterface(player, COFFIN_INTERFACE)
                set.content.forEachIndexed { index, itemId ->
                    sendItemOnInterface(player, COFFIN_INTERFACE, index + 3, itemId, 1)
                }
            }
            return@on true
        }

        /*
         * Handles read the gravestone.
         */

        on(GRAVESTONE_IDS, IntType.SCENERY, "read") { player, node ->
            GRAVESTONE_MAP[node.id]?.let { set ->
                openInterface(player, GRAVESTONE_INTERFACE)
                sendItemOnInterface(player, GRAVESTONE_INTERFACE, 2, set.item, 1)
            }
            return@on true
        }

        /*
         * Handles taking the coffin from grave.
         */

        on(GRAVE_IDS, IntType.SCENERY, "take-coffin") { player, node ->
            GRAVE_MAP[node.id]?.let { set ->
                val coffin = Item(set.coffinId)
                if (!hasSpaceFor(player, coffin)) {
                    sendMessage(player, "You need space in your inventory to take the coffin.")
                    return@on true
                } else {
                    lock(player, 3)
                    queueScript(player, 1, QueueStrength.NORMAL) {
                        player.animate(ANIMATION)
                        addItem(player, coffin.id, 1)
                        replaceScenery(node.asScenery(), set.emptyGraveId, -1)
                        return@queueScript stopExecuting(player)
                    }
                }
            }
            return@on true
        }

        /*
         * Handles placing the coffin into correct empty grave.
         */

        onUseWith(IntType.SCENERY, COFFIN_IDS, *EMPTY_GRAVE_IDS) { player, used, target ->
            COFFIN_MAP[used.id]?.let { set ->
                if (target.id == set.emptyGraveId) {
                    lock(player, 3)
                    queueScript(player, 1, QueueStrength.NORMAL) {
                        if (!removeItem(player, used.asItem())) return@queueScript stopExecuting(player)
                        player.animate(ANIMATION)
                        player.incrementAttribute(GameAttributes.GRAVEDIGGER_SCORE, 1)
                        replaceScenery(target.asScenery(), set.graveId, -1)
                        sendMessage(player, "You put the coffin into the grave.")
                        return@queueScript stopExecuting(player)
                    }
                }
            }
            return@onUseWith true
        }

        /*
         * Handles use woodcutting tools on dead tree.
         */

        onUseWith(IntType.SCENERY, WOODCUTTING_TOOLS, Scenery.DEAD_TREE_12732) { player, _, _ ->
            if (inBorders(player, EVENT_ZONE)) {
                sendMessages(player, "You don't need any wood.", "What are you planning on doing, making them a fresh coffin?")
            }
            return@onUseWith true
        }

        /*
         * Handles deposit at mausoleum.
         */

        on(MAUSOLEUM, IntType.SCENERY, "deposit") { player, _ ->
            player.bank.openDepositBox()
            player.bank.refreshDepositBoxInterface()
            return@on true
        }

        /*
         * Handles talk to Leo NPC.
         */

        on(NPCs.LEO_3508, IntType.NPC, "talk-to") { player, npc ->
            openDialogue(player, LeoDialogue(), npc)
            return@on true
        }
    }
}
