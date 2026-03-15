package content.data

import content.global.skill.magic.TeleportMethod
import content.global.skill.slayer.SlayerManager
import core.Util
import core.api.*
import core.game.dialogue.FaceAnim
import core.game.event.TeleportEvent
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds

/**
 * Represents enchanted jewellery items and their corresponding teleport options.
 */
enum class EnchantedJewellery(val options: Array<String>, val locations: Array<Location>, val crumbled: Boolean, vararg val ids: Int) {
    RING_OF_SLAYING(options = arrayOf("Sumona in Pollnivneach.", "Morytania Slayer Tower.", "Rellekka Slayer Caves.", "Tarn's Lair", "Nowhere. Give me a Slayer update."), locations = arrayOf(Location.create(3361, 2994, 0), Location.create(3428, 3535, 0), Location.create(2792, 3615, 0), Location.create(3424, 9660, 0)), crumbled = true, Items.RING_OF_SLAYING8_13281, Items.RING_OF_SLAYING7_13282, Items.RING_OF_SLAYING6_13283, Items.RING_OF_SLAYING5_13284, Items.RING_OF_SLAYING4_13285, Items.RING_OF_SLAYING3_13286, Items.RING_OF_SLAYING2_13287, Items.RING_OF_SLAYING1_13288),
    RING_OF_DUELING(options = arrayOf("Al Kharid Duel Arena.", "Castle Wars Arena.", "Fist of Guthix.", "Nowhere."), locations = arrayOf(Location.create(3314, 3235, 0), Location.create(2442, 3089, 0), Location.create(1693, 5600, 0)), crumbled = true, Items.RING_OF_DUELLING8_2552, Items.RING_OF_DUELLING7_2554, Items.RING_OF_DUELLING6_2556, Items.RING_OF_DUELLING5_2558, Items.RING_OF_DUELLING4_2560, Items.RING_OF_DUELLING3_2562, Items.RING_OF_DUELLING2_2564, Items.RING_OF_DUELLING1_2566),
    AMULET_OF_GLORY(options = arrayOf("Edgeville.", "Karamja.", "Draynor Village.", "Al-Kharid.", "Nowhere."), locations = arrayOf(Location.create(3087, 3495, 0), Location.create(2919, 3175, 0), Location.create(3104, 3249, 0), Location.create(3304, 3124, 0)), crumbled = false, Items.AMULET_OF_GLORY4_1712, Items.AMULET_OF_GLORY3_1710, Items.AMULET_OF_GLORY2_1708, Items.AMULET_OF_GLORY1_1706, Items.AMULET_OF_GLORY_1704),
    AMULET_OF_GLORY_T(options = AMULET_OF_GLORY.options, locations = AMULET_OF_GLORY.locations, crumbled = true, Items.AMULET_OF_GLORYT4_10354, Items.AMULET_OF_GLORYT3_10356, Items.AMULET_OF_GLORYT2_10358, Items.AMULET_OF_GLORYT1_10360, Items.AMULET_OF_GLORYT_10362),
    GAMES_NECKLACE(options = arrayOf("Burthorpe Games Room.", "Barbarian Outpost.", "Clan Wars.", "Wilderness Volcano.", "Corporeal Beast."), locations = arrayOf(Location.create(2899, 3563, 0), Location.create(2520, 3571, 0), Location.create(3266, 3686, 0), Location.create(3179, 3685, 0), Location.create(2885, 4372, 2)), crumbled = true, Items.GAMES_NECKLACE8_3853, Items.GAMES_NECKLACE7_3855, Items.GAMES_NECKLACE6_3857, Items.GAMES_NECKLACE5_3859, Items.GAMES_NECKLACE4_3861, Items.GAMES_NECKLACE3_3863, Items.GAMES_NECKLACE2_3865, Items.GAMES_NECKLACE1_3867),
    DIGSITE_PENDANT(options = arrayOf(), locations = arrayOf(Location.create(3342, 3445, 0)), crumbled = true, Items.DIGSITE_PENDANT_5_11194, Items.DIGSITE_PENDANT_4_11193, Items.DIGSITE_PENDANT_3_11192, Items.DIGSITE_PENDANT_2_11191, Items.DIGSITE_PENDANT_1_11190),
    COMBAT_BRACELET(options = arrayOf("Warriors' Guild", "Champions' Guild", "Monastery", "Ranging Guild", "Nowhere"), locations = arrayOf(Location.create(2878, 3546, 0), Location.create(3191, 3365, 0), Location.create(3051, 3489, 0), Location.create(2657, 3439, 0)), crumbled = false, Items.COMBAT_BRACELET4_11118, Items.COMBAT_BRACELET3_11120, Items.COMBAT_BRACELET2_11122, Items.COMBAT_BRACELET1_11124, Items.COMBAT_BRACELET_11126),
    SKILLS_NECKLACE(options = arrayOf("Fishing Guild.", "Mining Guild.", "Crafting Guild.", "Cooking Guild.", "Nowhere."), locations = arrayOf(Location.create(2611, 3392, 0), Location.create(3016, 3338, 0), Location.create(2933, 3290, 0), Location.create(3143, 3442, 0)), crumbled = false, Items.SKILLS_NECKLACE4_11105, Items.SKILLS_NECKLACE3_11107, Items.SKILLS_NECKLACE2_11109, Items.SKILLS_NECKLACE1_11111, Items.SKILLS_NECKLACE_11113),
    RING_OF_LIFE(options = arrayOf(), locations = arrayOf(), crumbled = true, Items.RING_OF_LIFE_2570),
    ;

    /**
     * Handles the use of the enchanted jewellery.
     */
    fun use(player: Player, item: Item, buttonID: Int, isEquipped: Boolean) {
        if (buttonID >= locations.size) {
            if (isSlayerRing(item)) {
                slayerProgressDialogue(player)
            }
            return
        }
        attemptTeleport(player, item, buttonID, isEquipped)
    }

    /**
     * Teleport pulse.
     */
    fun attemptTeleport(player: Player, item: Item, buttonID: Int, isEquipped: Boolean): Boolean {
        val itemIndex = getItemIndex(item)
        val nextJewellery = Item(getNext(itemIndex))

        if (!canTeleport(player, nextJewellery)) {
            return false
        }

        lock(player, 4)

        val location = if (this@EnchantedJewellery == RING_OF_LIFE)
            if (isDiaryComplete(player, DiaryType.ARDOUGNE, 2))
                Location.create(2662, 3307, 0)
            else
                player.getRespawnLocation()
        else
            getLocation(buttonID)

        closeAllInterfaces(player)
        queueScript(player, 0, QueueStrength.NORMAL) { stage ->
            when (stage) {
                0 -> {
                    player.impactHandler.disabledTicks = 4
                    visualize(player, ANIMATION, Graphics)
                    playGlobalAudio(player.location, Sounds.TP_ALL_200)
                    return@queueScript delayScript(player, 3)
                }
                1 -> {
                    teleport(player, location)
                    resetAnimator(player)
                    handleJewelleryUsage(player, item, nextJewellery, itemIndex, isEquipped, location)
                    return@queueScript stopExecuting(player)
                }

                else -> return@queueScript stopExecuting(player)
            }
        }

        return true
    }

    /**
     * Handles the usage of the jewellery after teleportation.
     */
    private fun handleJewelleryUsage(
        player: Player,
        item: Item,
        nextID: Item,
        itemIndex: Int,
        isEquipped: Boolean,
        location: Location
    ) {
        val jewelleryName = when {
            "ring" in item.name.lowercase() -> "ring's"
            "combat" in item.name.lowercase() -> "bracelet's"
            "necklace" in item.name.lowercase() -> "necklace's"
            else -> "amulet's"
        }

        val isLast = isLastItemIndex(itemIndex)
        val message = if (isLast) {
            "You use your $jewelleryName last charge."
        } else {
            val remainingUses = ids.size - itemIndex - 1
            "Your ${getJewelleryType(item)} has ${Util.convert(remainingUses)} uses left."
        }

        if (isLast) {
            if (crumbled) {
                crumbleJewellery(player, item, isEquipped)
            } else {
                sendMessage(player, "You will need to recharge your ${getJewelleryType(item)} before you can use it again.")
            }
        } else {
            replaceJewellery(player, item, nextID, isEquipped)
        }

        unlock(player)
        sendMessage(player, message)
        player.dispatch(TeleportEvent(TeleportManager.TeleportType.NORMAL, TeleportMethod.JEWELRY, item, location))
    }

    /**
     * Replaces the current jewellery item with the next one.
     */
    private fun replaceJewellery(player: Player, item: Item, nextID: Item, isEquipped: Boolean) {
        if (isEquipped) {
            replaceSlot(player, item.slot, nextID, item, Container.EQUIPMENT)
        } else {
            replaceSlot(player, item.slot, nextID)
        }
    }

    /**
     * Crumbles the jewellery item and removes it from the inventory.
     */
    private fun crumbleJewellery(player: Player, item: Item, isEquipped: Boolean) {
        if (isEquipped) {
            removeItem(player, item, Container.EQUIPMENT)
        } else {
            removeItem(player, item)
        }
        if (isSlayerRing(item)) {
            sendMessage(player, "The ring collapses into a Slayer gem, which you stow in your pack.")
            addItem(player, Items.ENCHANTED_GEM_4155)
        }
    }

    /**
     * Checks if the item is a slayer ring.
     */
    private fun isSlayerRing(item: Item): Boolean = item.id in RING_OF_SLAYING.ids

    /**
     * Sends the slayer progress dialogue to the player.
     */
    private fun slayerProgressDialogue(player: Player) {
        val slayerManager = SlayerManager.getInstance(player)
        if (!slayerManager.hasTask()) {
            sendNPCDialogue(player, slayerManager.master!!.npc, "You need something new to hunt. Come and see me when you can and I'll give you a new task.", FaceAnim.HALF_GUILTY)
            return
        }
        sendNPCDialogue(
            player,
            slayerManager.master!!.npc,
            "You're currently assigned to kill ${getSlayerTaskName(player).lowercase()}'s; only ${
                getSlayerTaskKillsRemaining(player)
            } more to go.",
            FaceAnim.FRIENDLY,
        )
        setVarp(player, 2502, slayerManager.flags.taskFlags shr 4)
    }

    /**
     * Checks if the player can teleport using the item.
     */
    private fun canTeleport(player: Player, item: Item): Boolean = player.zoneMonitor.teleport(1, item)

    /**
     * Gets the next item in the sequence.
     */
    fun getNext(index: Int): Int = if (index + 1 > ids.size - 1) ids.last() else ids[index + 1]

    /**
     * Gets the location associated with the given index.
     */
    private fun getLocation(index: Int): Location = if (index >= locations.size) locations.last() else locations[index]

    /**
     * Gets the name of the jewellery item, with any suffix removed.
     */
    fun getJewelleryName(item: Item): String = item.name.replace(""" ?\(t?[0-9]?\)""".toRegex(), "")

    /**
     * Gets jewellery type.
     */
    fun getJewelleryType(item: Item): String =
        when {
            this == GAMES_NECKLACE -> "games necklace"
            this == DIGSITE_PENDANT -> "digsite pendant"
            this == COMBAT_BRACELET -> "combat bracelet"
            this == SKILLS_NECKLACE -> "skill's necklace"
            this == AMULET_OF_GLORY || this == AMULET_OF_GLORY_T -> "amulet of glory"
            this == RING_OF_SLAYING -> "ring of slaying"
            this == RING_OF_DUELING -> "ring of dueling"
            else -> item.name.split(" ")[0].lowercase()
        }

    /**
     * Checks if the given index is the last item index in the sequence.
     */
    fun isLastItemIndex(index: Int): Boolean = index == ids.size - 1

    /**
     * Gets the index of the item in the jewellery sequence.
     */
    fun getItemIndex(item: Item): Int = ids.indexOf(item.id)

    companion object {
        private val ANIMATION = Animation(Animations.ANIMATION_9603)
        private val Graphics = Graphics(shared.consts.Graphics.TP_RING_OF_DUELING_1684)
        val idMap = HashMap<Int, EnchantedJewellery>()

        init {
            values().forEach { entry ->
                entry.ids.forEach { idMap[it] = entry }
            }
        }
    }
}
