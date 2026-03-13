package content.region.kandarin.yanille.quest.itwatchtower.plugin

import content.data.GameAttributes
import content.data.skill.SkillingTool
import content.region.kandarin.yanille.quest.itwatchtower.dialogue.BattlementDialogue
import content.region.kandarin.yanille.quest.itwatchtower.dialogue.CityGuardDialogue
import content.region.kandarin.yanille.quest.itwatchtower.dialogue.OgreGuardNorthWestGateDialogue
import content.region.kandarin.yanille.quest.itwatchtower.dialogue.OgreGuardSouthEastGateDialogue
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.global.action.ClimbActionHandler
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.combat.ImpactHandler
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.node.entity.player.link.WarningHandler
import core.game.node.entity.player.link.WarningManager
import core.game.node.entity.player.link.Warnings
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.update.flag.context.Animation
import core.tools.END_DIALOGUE
import shared.consts.*

class WatchTowerPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles try to climb the ladder.
         */

        on(Scenery.LADDER_2833, IntType.SCENERY, "climb-up") { player, _ ->
            if (getQuestStage(player, Quests.WATCHTOWER) >= 1) {
                sendNPCDialogue(player, NPCs.TOWER_GUARD_877, "It is the wizards' helping hand - let 'em up.", FaceAnim.FRIENDLY)
                addDialogueAction(player) { _, _ ->
                    ClimbActionHandler.climb(player, Animation(Animations.HUMAN_CLIMB_STAIRS_828), Location.create(2544, 3112, 1))
                }
            } else {
                sendNPCDialogue(player, NPCs.TOWER_GUARD_877, "You can't go up there. That's private, that is.", FaceAnim.ANNOYED)
            }
            return@on true
        }

        /*
         * Handles talking to Tower guards.
         */

        on(NPCs.TOWER_GUARD_877, IntType.NPC, "talk-to") { player, _ ->
            sendPlayerDialogue(player, "Hello. What are you doing here?", FaceAnim.HALF_ASKING)
            addDialogueAction(player) { _, _ ->
                sendNPCDialogue(player, NPCs.TOWER_GUARD_877, "We are the tower guards - our business is our own!", FaceAnim.ANNOYED)
            }
            return@on true
        }

        val bushes =
            mapOf(
                Scenery.BUSH_2798 to null,
                Scenery.BUSH_2799 to Pair(Items.FINGERNAILS_2384, "What's this? Disgusting! Some fingernails. They may be a clue though... I'd better take them."),
                Scenery.BUSH_2800 to Pair(Items.DAMAGED_DAGGER_2387, "Aha, a dagger! I wonder if this is evidence..."),
                Scenery.BUSH_2801 to Pair(Items.TATTERED_EYE_PATCH_2388, "I've found an eye patch; I had better show this to the Watchtower Wizard."),
                Scenery.BUSH_2802 to Pair(Items.OLD_ROBE_2385, "Aha! A robe. This could be a clue..."),
                Scenery.BUSH_2803 to Pair(Items.UNUSUAL_ARMOUR_2386, "Here's some armour; it could be evidence..."),
            )

        /*
         * Handles searching the bushes for quest items.
         */

        bushes.forEach { (bush, item) ->
            on(bush, IntType.SCENERY, "search") { player, _ ->
                lock(player, 1)
                searchBush(player, item)
                return@on true
            }
        }

        /*
         * Handles enter to skavid caves.
         * https://imgur.com/a/JIt7MVO
         */

        on(SKAVID_CAVE_ENTRANCE, IntType.SCENERY, "enter") { player, node ->
            val location = when(node.id){
                Scenery.CAVE_ENTRANCE_2805 -> Location(2498, 9418, 0)
                Scenery.CAVE_ENTRANCE_2806 -> Location(2530, 9467, 0)
                Scenery.CAVE_ENTRANCE_2807 -> Location(2518, 9454, 0)
                Scenery.CAVE_ENTRANCE_2808 -> Location(2498, 9451, 0)
                Scenery.CAVE_ENTRANCE_2809 -> Location(2504, 9440, 0)
                Scenery.CAVE_ENTRANCE_2810 -> Location(2522, 9411, 0)
                else -> null
            }

            if(!inInventory(player, Items.SKAVID_MAP_2376) || getQuestStage(player, Quests.WATCHTOWER) < 10) {
                lock(player, 6)
                openInterface(player, Components.FADE_TO_BLACK_115)
                sendMessage(player, "There's no way I can find my way through without a map of some kind.")
                runTask(player, 6) {
                    openInterface(player, Components.FADE_FROM_BLACK_170)
                    teleport(player, ENTRANCE_LOCATION.random(), TeleportManager.TeleportType.INSTANT)
                }
                return@on true
            }

            openDialogue(player, SkavidEntranceDialogue(location))
            return@on true
        }

        /*
         * Handles hole entrance to Yanille (One-way).
         */

        on(Scenery.HOLE_2823, IntType.SCENERY, "climb-down") { player, _ ->
            teleport(player, Location(2588, 3106, 0), TeleportManager.TeleportType.INSTANT)
            return@on true
        }

        /*
         * Handles exit from north skavid caves.
         */

        on(SKAVID_CAVE_EXIT, IntType.SCENERY, "leave") { player, node ->
            if(getAttribute(player, GameAttributes.WATCHTOWER_DARK_AREA, false)) {
                sendMessage(player, "You need to find other way to exit from there.")
                return@on true
            }
            val location = when(node.id) {
                Scenery.CAVE_EXIT_2817 -> ENTRANCE_LOCATION[0]
                Scenery.CAVE_EXIT_2818 -> ENTRANCE_LOCATION[1]
                Scenery.CAVE_EXIT_2819 -> ENTRANCE_LOCATION[2]
                Scenery.CAVE_EXIT_2820 -> ENTRANCE_LOCATION[3]
                Scenery.CAVE_EXIT_2821 -> ENTRANCE_LOCATION[4]
                Scenery.CAVE_EXIT_2822 -> ENTRANCE_LOCATION[5]
                else -> null
            }

            if (location != null) {
                teleport(player, location, TeleportManager.TeleportType.INSTANT)
            }
            return@on true
        }

        /*
         * Handles exit the cave without lit.
         */

        on(Scenery.ROCK_2837, IntType.SCENERY, "search") { player, _ ->
            sendMessage(player, "You search the rock...")
            sendMessage(player, "You uncover a tunnel entrance.")
            lock(player, 3)
            runTask(player, 2) {
                teleport(player, Location(2554, 3054, 0), TeleportManager.TeleportType.INSTANT)
                sendPlayerDialogue(player, "Phew! At last I'm out..." + "Next time I will take some light!", FaceAnim.HALF_GUILTY)
                removeAttribute(player, GameAttributes.WATCHTOWER_DARK_AREA)
            }
            return@on true
        }

        /*
         * Handles interaction with tobans chest.
         */

        on(Scenery.CHEST_2790, IntType.SCENERY, "open")  { player, node ->
            handleTobansChest(player, node.asScenery())
            return@on true
        }

        /*
         * Handles use the key on the tobans chest.
         */

        onUseWith(IntType.SCENERY, Items.TOBANS_KEY_2378, Scenery.CHEST_2790) { player, _, scenery ->
            handleTobansChest(player, scenery.asScenery())
            return@onUseWith true
        }

        /*
         * Handles opening skavid map.
         */

        on(Items.SKAVID_MAP_2376, IntType.ITEM, "read") { player, _ ->
            openInterface(player, Components.SKAVID_MAP_479)
            return@on true
        }

        /*
         * Handles entrance to grew island.
         */

        on(Scenery.CAVE_ENTRANCE_2811, IntType.SCENERY, "enter") { player, _ ->
            sendMessage(player, "You enter the cave.")
            teleport(player, Location(2576, 3029, 0))
            sendPlayerDialogue(player, "Wow! That tunnel went a long way.", FaceAnim.EXTREMELY_SHOCKED)
            return@on true
        }

        /*
         * Handles ladder from grew island.
         */

        on(Scenery.LADDER_2812, IntType.SCENERY, "climb-down") { player, _ ->
            sendMessage(player, "You climb down the ladder.")
            teleport(player, Location(2499, 2988, 0))
            return@on true
        }

        /*
         * Handles use any one relic part with another.
         */

        onUseWith(IntType.NPC, Items.RELIC_PART_1_2373, Items.RELIC_PART_2_2374, Items.RELIC_PART_3_2375) { player, _, _ ->
            sendMessage(player, "I think these fit together, but I can't seem to make them fit.")
            sendMessage(player, "I am going to need someone with experience to help me with this.")
            return@onUseWith true
        }

        /*
         * Handles Gorad NPC attack option.
         */

        on(NPCs.GORAD_856, IntType.NPC, "attack") { player, node ->
            if (isQuestComplete(player, Quests.WATCHTOWER)) {
                sendNPCDialogueLines(player, node.id, FaceAnim.OLD_DEFAULT, false, "Ho Ho! why would I want to fight a worm?", "Get lost!")
            } else {
                player.attack(node)
            }
            return@on true
        }

        /*
         * Handles move through north-west ogre city gates.
         */

        on(OGRE_CITY_NW_GATE, IntType.SCENERY, "open") { player, node ->
            if(isQuestComplete(player, Quests.WATCHTOWER) || getAttribute(player, GameAttributes.WATCHTOWER_GATE_UNLOCK, false)) {
                if (player.location.x < 2504) {
                    sendNPCDialogue(player, NPCs.OGRE_GUARD_859, "It's the small creature; you may pass.", FaceAnim.OLD_DEFAULT)
                }
                DoorActionHandler.handleAutowalkDoor(player, node.asScenery())
            } else {
                openDialogue(player, OgreGuardNorthWestGateDialogue())
            }
            return@on true
        }

        /*
         * Handles move through south-east ogre city gates.
         */

        on(OGRE_CITY_SE_GATE, IntType.SCENERY, "open") { player, node ->
            if(isQuestComplete(player, Quests.WATCHTOWER) || getAttribute(player, GameAttributes.WATCHTOWER_GOLD_GATE_UNLOCK, false)) {
                DoorActionHandler.handleAutowalkDoor(player, node.asScenery())
            } else {
                openDialogue(player, OgreGuardSouthEastGateDialogue())
            }
            return@on true
        }

        /*
         * Handles use the relic on ogre guards.
         */

        onUseWith(IntType.NPC, Items.OGRE_RELIC_2372, NPCs.OGRE_GUARD_859) { player, _, _ ->
            openDialogue(player, OgreGuardNorthWestGateDialogue())
            return@onUseWith true
        }

        /*
         * Handles use the gold bar on ogre city guards.
         */

        onUseWith(IntType.NPC, Items.GOLD_BAR_2357, NPCs.OGRE_GUARD_858) { player, _, _ ->
            openDialogue(player, OgreGuardSouthEastGateDialogue())
            return@onUseWith true
        }

        /*
         * Handles climb over battlement.
         */

        on(Scenery.BATTLEMENT_2832, IntType.SCENERY, "climb-over") { player, _ ->
            if(!isQuestComplete(player, Quests.WATCHTOWER)) {
                openDialogue(player, BattlementDialogue())
            } else {
                val destination = if(player.location.x > 2506) player.location.transform(-2,0,0) else player.location.transform(2,0,0)
                forceMove(player, player.location, destination, 0, 90, null, Animations.CLIMB_OVER_THING_5038)
                sendMessage(player, "You climb over the low wall.")
            }
            return@on true
        }

        /*
         * Handles talking to ogres near battlement.
         */

        on(NPCs.OGRE_GUARD_860, IntType.NPC, "talk-to") { player, node ->
            if(isQuestComplete(player, Quests.WATCHTOWER)) {
                sendNPCDialogue(player, node.id, "It's that creature again. This time we will let it go.", FaceAnim.OLD_DEFAULT)
            } else {
                openDialogue(player, BattlementDialogue())
            }
            return@on true
        }

        /*
         * Handles give the rock cake to ogre guards near battlement.
         */

        onUseWith(IntType.NPC, Items.ROCK_CAKE_2379, NPCs.OGRE_GUARD_860) { player, _, with ->
            if (!inInventory(player, Items.ROCK_CAKE_2379)) return@onUseWith true

            if(isQuestComplete(player, Quests.WATCHTOWER)) {
                sendMessage(player, "Nothing interesting happens.")
                return@onUseWith true
            }

            if (removeItem(player, Items.ROCK_CAKE_2379)) {
                sendMessage(player, "You give the guard a rock cake.")
                openDialogue(player, OgreGuardDialogue(), with.id)
            } else {
                sendMessage(player, "You don't have a rock cake.")
            }
            return@onUseWith true
        }

        /*
         * Handles talking to ogre guards near bridge.
         */

        on(NPCs.OGRE_GUARD_861, IntType.NPC, "talk-to") { player, node ->
            sendNPCDialogue(player, node.id, "What do you want, small thing? Leave us alone!", FaceAnim.OLD_DEFAULT)
            return@on true
        }

        /*
         * Handles jump over the gap.
         */

        on(Scenery.GAP_2830, IntType.SCENERY, "jump-over") { player, _ ->
            val npc = NPC(NPCs.OGRE_GUARD_861)
            openDialogue(player, GapDialogue(), npc)
            return@on true
        }

        /*
         * Handles reverse jump over the gap.
         */

        on(Scenery.GAP_2831, IntType.SCENERY, "jump-over") { player, _ ->
            val dx = if (player.location.x >= 2531) -1 else 1
            forceMove(player, player.location, player.location.transform(dx, -3, 0), 0, 90, null, Animations.JUMP_OBSTACLE_5355) {
                sendPlayerDialogue(player, "I'm glad that was easier on the way back!")
                sendMessage(player, "You daringly jump across the chasm.")
            }
            return@on true
        }

        /*
         * Handles talking to ogre trader near rock cake stall.
         */

        on(NPCs.OGRE_TRADER_875, IntType.NPC, "talk-to") { player, node ->
            // sendNPCDialogue(player, node.id, "Grr! get your hands off those cakes!", FaceAnim.OLD_DEFAULT)
            sendNPCDialogue(player, node.id, "Arr, small thing wants my food, does it?", FaceAnim.OLD_DEFAULT)
            return@on true
        }


        /*
         * Handles talking to ogre chieftain.
         */

        on(NPCs.OGRE_CHIEFTAIN_852, IntType.NPC, "talk-to") { player, node ->
            sendNPCDialogue(player, node.id, "Arr, small thing wants my food, does it?", FaceAnim.OLD_DEFAULT)
            runTask(player, 3) {
                sendNPCDialogue(player, node.id, "I'll teach you to deal with ogres!", FaceAnim.OLD_DEFAULT)
                RegionManager.getLocalNpcs(player).firstOrNull { it.id == node.id }?.attack(player)
                sendMessage(player, "You are under attack!")
            }
            return@on true
        }

        /*
         * Handles talking to city guard.
         */

        on(NPCs.CITY_GUARD_862, IntType.NPC, "talk-to") { player, _ ->
            openDialogue(player, CityGuardDialogue())
            return@on true
        }

        /*
         * Handles solving the riddle (gives death rune to city guard).
         */

        onUseWith(IntType.NPC, Items.DEATH_RUNE_560, NPCs.CITY_GUARD_862) { player, _, _ ->
            openDialogue(player, CityGuardDialogue())
            return@onUseWith true
        }

        /*
         * Handles reading the spell scroll.
         */

        on(Items.SPELL_SCROLL_2396, IntType.ITEM, "read") { player, node ->
            if(!inInventory(player, Items.SPELL_SCROLL_2396)) return@on true
            animate(player, Animations.READING_SCROLL_DISPLACED_WATCH_TOWER_5354)
            sendDialogue(player, "You memorise what is written on the scroll.")
            runTask(player, 3) {
                removeItem(player, node.asItem(), Container.INVENTORY)
                sendDialogueLines(player, "You can now cast the Watchtower teleport spell... ...Provided you", "have the required runes and magic level.")
                setAttribute(player, GameAttributes.WATCHTOWER_TELEPORT, true)
                sendMessage(player, "The scroll crumbles to dust.")
            }
            return@on true
        }

        /*
         * Handles entrance to enclave cave.
         */

        on(Scenery.CAVE_ENTRANCE_2804, IntType.SCENERY, "enter") { player, _ ->
            sendNPCDialogue(player, NPCs.ENCLAVE_GUARD_870, "No you don't!", FaceAnim.OLD_DEFAULT)
            return@on true
        }

        /*
         * Handles using cave nightshade on an enclave guard.
         */

        onUseWith(IntType.NPC, Items.CAVE_NIGHTSHADE_2398, NPCs.ENCLAVE_GUARD_870) { player, _, npc ->
            sendNPCDialogueLines(player, npc.id, FaceAnim.OLD_DEFAULT, false, "What is this? Arrrrgh! I cannot stand this plant! Argh,", "it burns! It burns!")
            addDialogueAction(player) { _, _ ->
                if (!WarningManager.isWarningDisabled(player, Warnings.WATCHTOWER_SHAMAN_CAVE)) {
                    WarningManager.openWarningInterface(player, Warnings.WATCHTOWER_SHAMAN_CAVE)
                    return@addDialogueAction
                } else {
                    WarningHandler.handleWatchtower(player)
                }
            }
            return@onUseWith true
        }

        /*
         * Handles bad mix of magic ogre potion.
         */

        onUseWith(IntType.ITEM, Items.CLEAN_GUAM_249, Items.VIAL_2389) { player, used, with ->
            if(removeItem(player, used.asItem()) && removeItem(player, with.asItem())) {
                impact(player, 5, ImpactHandler.HitsplatType.NORMAL)
                sendGraphics(Graphics.FIRE_WAVE_IMPACT_157, player.location)
                addItem(player, Items.VIAL_229)
            }
            return@onUseWith true
        }

        /*
         * Handles using Ogre potion on Watchtower wizard after the quest.
         */

        onUseWith(IntType.NPC, OGRE_POTIONS, NPCs.WATCHTOWER_WIZARD_872) { player, used, with ->
            if (isQuestComplete(player, Quests.WATCHTOWER)) {
                openDialogue(player, WatchtowerDialogue(used.asItem()))
                return@onUseWith true
            }
            return@onUseWith false
        }

        /*
         * Handles using ground bat bones on vial of water.
         */

        onUseWith(IntType.ITEM, Items.GROUND_BAT_BONES_2391, Items.VIAL_OF_WATER_227) { player, used, with ->
            if(removeItem(player, used.asItem())){
                replaceSlot(player, with.index, Item(Items.VIAL_229, 1))
                sendMessage(player, "The water from your vial evaporate.")
            }
            return@onUseWith true
        }

        /*
         * Handles creating potion.
         */

        onUseWith(IntType.ITEM, Items.GROUND_BAT_BONES_2391, Items.VIAL_2390) { player, used, with ->
            if(removeItem(player, used.asItem()) && removeItem(player, with.asItem())){
                addItem(player, Items.POTION_2394, 1)
                sendMessage(player, "You produce a strong potion.")
            }
            return@onUseWith true
        }

        /*
         * Handles using the magic ogre potions on shamans.
         */

        onUseWith(IntType.NPC, Items.MAGIC_OGRE_POTION_2395, *OGRE_SHAMAN) { player, _, npc ->
            stopWalk(player)
            val anim = Animation(Animations.POURING_MAGIC_OGRE_POTION_WATCH_TOWER_5361)

            animate(player, anim)

            // animate(findLocalNPC(player, npc.id)!!, ???)

            sendMessage(player, "There is a bright flash!")
            sendMessage(player, "The ogre dissolves into spirit form.")

            val count = player.getAttribute(GameAttributes.WATCHTOWER_OGRE_DESTROY_COUNT) ?: 0
            val killCount = count + 1

            player.setAttribute(GameAttributes.WATCHTOWER_OGRE_DESTROY_COUNT, killCount)

            queueScript(player, anim.duration, QueueStrength.WEAK) {
                npc.asNpc().clear()
                if (killCount >= 6) {
                    setQuestStage(player, Quests.WATCHTOWER, 90)
                    removeAttribute(player, GameAttributes.WATCHTOWER_OGRE_DESTROY_COUNT)
                    sendItemDialogue(player, Items.CRYSTAL_2382, "A crystal drops from the hand of the dissappearing ogre. You snatch it up quickly.")
                    addItemOrDrop(player, Items.CRYSTAL_2382, 1)
                } else {
                    sendPlayerDialogue(player, "That's $killCount destroyed...")
                }
                return@queueScript stopExecuting(player)
            }

            return@onUseWith true
        }

        /*
         * Handles prospecting the rock of dalgoth.
         */

        on(Scenery.ROCK_OF_DALGROTH_2816, IntType.SCENERY, "prospect") { player, _ ->
            sendMessage(player, "You examine the rock for ogres...")
            sendMessage(player, "The rock contains crystal!", 1)
            return@on true
        }

        /*
         * Handles mine the rock of dalgoth.
         */

        on(Scenery.ROCK_OF_DALGROTH_2816, IntType.SCENERY, "mine") { player, _ ->
            if (SkillingTool.getPickaxe(player) == null) {
                sendMessage(player, "You do not have a pickaxe to use.")
                return@on true
            }

            if(getStatLevel(player, Skills.MINING) < 40) {
                sendMessage(player, "You need a Mining level of 40 to mine this rock.")
                return@on true
            }

            animate(player, SkillingTool.getPickaxe(player)!!.animation)
            sendItemDialogue(player, Items.CRYSTAL_2383, "A crack appears in the rock and you prise a crystal out.")
            addItem(player, Items.CRYSTAL_2383, 1)
            return@on true
        }

        /*
         * Handles use crystal on pillar.
         */

        onUseWith(IntType.SCENERY, CRYSTALS, *PILLARS) { player, used, with ->
            val success = when {
                used.id == Items.CRYSTAL_2383 && with.id == Scenery.PILLAR_21546 -> {
                    removeItem(player, used.asItem()).also {
                        if (it) setVarbit(player, 3130, 1, true) }
                }
                used.id == Items.CRYSTAL_2382 && with.id == Scenery.PILLAR_20022 -> {
                    removeItem(player, used.asItem()).also {
                        if (it) setVarbit(player, Vars.VARBIT_QUEST_WATCHTOWER_NW_PILLAR_3127, 1, true) }
                }
                used.id == Items.CRYSTAL_2381 && with.id == Scenery.PILLAR_20030 -> {
                    removeItem(player, used.asItem()).also {
                        if (it) setVarbit(player, Vars.VARBIT_QUEST_WATCHTOWER_SE_PILLAR_3129, 1, true) }
                }
                used.id == Items.CRYSTAL_2380 && with.id == Scenery.PILLAR_20026 -> {
                    removeItem(player, used.asItem()).also {
                        if (it) setVarbit(player, Vars.VARBIT_QUEST_WATCHTOWER_SW_PILLAR_3128, 1, true) }
                }
                else -> false
            }

            if (!success) {
                sendMessage(player, "That is not the right crystal for this pillar; try another.")
            }

            return@onUseWith true
        }

        /*
         * Handles pulling the lever.
         */

        on(Scenery.LEVER_2794, IntType.SCENERY, "pull") { player, node ->
            val npc = NPC(NPCs.WATCHTOWER_WIZARD_872)
            sendMessage(player,"You pull the lever...")

            if(isQuestComplete(player, Quests.WATCHTOWER) || getQuestStage(player, Quests.WATCHTOWER) == 0) {
                val message = when(node.location) {
                    Location.create(2927, 4715, 2) -> "The lever is stuck in the down position."
                    else -> "It had no effect."
                }
                sendMessage(player, message)
                return@on true
            }

            for (i in (3027..3030)) if (getVarbit(player, i) != 1) {
                sendMessage(player,"You need to put the crystals on the correct pillars before the shield will work.")
                sendMessage(player,"All that happens now is a rather unsatisfying clunking noise.")
                return@on true
            }

            teleport(player, Location.create(2928, 4715, 2))
            sendMessage(player, "The magic force field activates.")
            openDialogue(player, LeverDialogue(), npc)
            return@on true
        }

        /*
         * Handles exit from ogres enclave.
         */

        on(Scenery.CAVE_EXIT_32494, IntType.SCENERY, "exit") { player, node ->
            if(node.location.x == 2598 && node.location.y == 9470) {
                teleport(player, Location.create(2541, 3054, 0), TeleportManager.TeleportType.INSTANT)
            } else {
                teleport(player, Location.create(2781, 2935, 0), TeleportManager.TeleportType.INSTANT)
                sendMessage(player, "You crawl back out of the cavern...")
            }
            return@on true
        }

    }

    /**
     * Represents the Watchtower dialogue.
     */
    inner class WatchtowerDialogue(val used : Item) : DialogueFile() {
        override fun handle(componentID: Int, buttonID: Int) {
            when(stage) {
                0 -> npc(FaceAnim.HALF_ASKING, "Another potion? Ooo no, I don't think so...")
                1 -> npc(FaceAnim.WORRIED, "I can't let you use this anymore, it is just too dangerous", "I'd better take it from you before you injure yourself.")
                2 -> {
                    end()
                    if (inInventory(player!!, used.id)) {
                        removeItem(player!!, used.asItem())
                    }
                }
            }
        }
    }

    /**
     * Lever Dialogue (Watchtower Quest reward).
     */
    inner class LeverDialogue : DialogueFile() {

        override fun handle(componentID: Int, buttonID: Int) {
            when (stage) {
                0 -> npc(FaceAnim.HAPPY, "Marvellous!, It works! The town will now be safe.").also { stage++ }
                1 -> npc(FaceAnim.HAPPY, "Your help was invaluable. Take this payment as a token", "of my gratitude.").also { stage++ }
                2 -> npcl(FaceAnim.HAPPY, "Also, let me improve your Magic level for you.").also { stage++ }
                3 -> npc(FaceAnim.HAPPY, "Here is a special item for you - it's a new spell. Read", "the scroll and you will be able to teleport yourself here.").also { stage++ }
                4 -> end().also { finishQuest(player!!, Quests.WATCHTOWER) }
            }
        }
    }

    /**
     * Represents the ogre guard dialogue.
     * (Wall passage with rock cake bribe).
     */
    inner class OgreGuardDialogue : DialogueFile() {

        override fun handle(componentID: Int, buttonID: Int) {
            when (stage) {
                0 -> player(FaceAnim.HALF_THINKING, "How about this?").also { stage++ }
                1 -> npc(FaceAnim.OLD_DEFAULT, "Well, well, look at this. My favourite: rock cake! Okay,", "we will let it through.").also { stage++ }
                2 -> {
                    end ()
                    val startLocation = Location.create(2506, 3012, 0)
                    val endLocation = Location.create(2508, 3012, 0)
                    forceMove(player!!, startLocation, endLocation, 60, 150, null, Animations.CLIMB_OVER_THING_5038)
                    sendMessage(player!!, "You climb over the low wall.")
                }
            }
        }
    }

    /**
     * Represents the skavid entrance dialogue.
     * (Cave entrance check for light source).
     */
    inner class SkavidEntranceDialogue(private val location: Location? = null) : DialogueFile() {

        override fun handle(componentID: Int, buttonID: Int) {
            when (stage) {
                0 -> sendDialogueLines(player!!, "If your light source goes out down there you'll be in trouble! Are", "you sure you want to go in without a tinderbox?").also { stage++ }
                1 -> options("I'll be fine without a tinderbox.", "I'll come back with a tinderbox.").also { stage++ }
                2 -> when(buttonID) {
                    1 -> {
                        end()
                        if (location != null) {
                            teleport(player!!, location, TeleportManager.TeleportType.INSTANT)
                            sendMessage(player!!, "You enter the cave...")
                            setAttribute(player!!, "watchtower-cave", true)
                        }
                    }
                    2 -> end()
                }
            }
        }
    }

    /**
     * Represents the gap dialogue.
     * (bridge crossing with toll payment).
     */
    inner class GapDialogue : DialogueFile() {

        override fun handle(componentID: Int, buttonID: Int) {
            when (stage) {
                0 -> npc(FaceAnim.OLD_DEFAULT, "Oi! Little thing. If you want to cross here, you must", "pay me 20 gold pieces first!").also { stage++ }
                1 -> player(FaceAnim.HALF_ASKING, "You want me to give you 20 gold pieces to let me", "jump off a bridge?").also { stage++ }
                2 -> npcl(FaceAnim.OLD_DEFAULT, "That's what I said, like it or lump it.").also { stage++ }
                3 -> options("Okay, I'll pay it.", "Forget it, I'm not paying.").also { stage++ }
                4 -> when (buttonID) {
                    1 -> if (!removeItem(player!!, Item(Items.COINS_995, 20))){
                        npcl(FaceAnim.OLD_DEFAULT, "You don't have enough gold pieces!")
                        stage = END_DIALOGUE
                    } else {
                        player(FaceAnim.HALF_ASKING, "Okay, I'll pay it.").also { stage = 5 }
                    }
                    2 -> playerl(FaceAnim.HALF_ASKING, "Forget it, I'm not paying.").also { stage = 7 }

                }
                5 -> npcl(FaceAnim.OLD_DEFAULT, "A wise choice, little thing.").also { stage++ }
                6 -> {
                    end ()
                    val dx = if (player!!.location.x >= 2531) -1 else 1
                    forceMove(player!!, player!!.location, player!!.location.transform(dx, 3, 0), 0, 90, null, Animations.JUMP_OBSTACLE_5355) {
                        sendMessage(player!!, "You daringly jump across the chasm.")
                        sendPlayerDialogue(player!!, "Phew! I just made it.")
                    }
                }
                7 -> {
                    end()
                    npcl(FaceAnim.OLD_DEFAULT, "In that case you're not crossing.")
                    sendMessage(player!!, "The guard blocks your path.")
                }
            }
        }
    }

    companion object {
        val OGRE_CITY_NW_GATE = intArrayOf(Scenery.CITY_GATE_2788, Scenery.CITY_GATE_2789)
        val OGRE_CITY_SE_GATE = intArrayOf(Scenery.CITY_GATE_2786, Scenery.CITY_GATE_2787)
        val SKAVID_CAVE_ENTRANCE = (Scenery.CAVE_ENTRANCE_2805..Scenery.CAVE_ENTRANCE_2810).toIntArray()
        val SKAVID_CAVE_EXIT = (Scenery.CAVE_EXIT_2817..Scenery.CAVE_EXIT_2822).toIntArray()
        val ENTRANCE_LOCATION = arrayOf(Location(2563, 3024, 0), Location(2524, 3070, 0), Location(2541, 3054, 0), Location(2554, 3054, 0), Location(2552, 3035, 0), Location(2529, 3012, 0))
        val OGRE_POTIONS = intArrayOf(Items.POTION_2394,Items.VIAL_2389, Items.VIAL_2390)
        val OGRE_SHAMAN = intArrayOf(5183,5180,5175,5186,5192,5189)
        val CRYSTALS = intArrayOf(Items.CRYSTAL_2383,Items.CRYSTAL_2382,Items.CRYSTAL_2381,Items.CRYSTAL_2380)
        val PILLARS = intArrayOf(Scenery.PILLAR_21546,Scenery.PILLAR_20022,Scenery.PILLAR_20026,Scenery.PILLAR_20030)

        /**
         * Searches a bush for a quest-related item.
         */
        private fun searchBush(player: Player, item: Pair<Int, String>?): Boolean {
            if (item == null || getQuestStage(player, Quests.WATCHTOWER) < 1) {
                sendPlayerDialogue(player, "Hmmm, nothing here.", FaceAnim.NEUTRAL)
                return true
            }

            if (!inInventory(player, item.first)) {
                sendPlayerDialogue(player, item.second, FaceAnim.NEUTRAL)
                addItem(player, item.first)
                return true
            }

            sendPlayerDialogue(player, "I have already searched this place.", FaceAnim.NEUTRAL)
            return true
        }

        /**
         * Handles the player interacting with Tobans chest.
         */
        private fun handleTobansChest(player: Player, scenery: core.game.node.scenery.Scenery? = null) {
            if (!inInventory(player, Items.TOBANS_KEY_2378)) {
                sendMessage(player, "The chest is locked.")
                sendPlayerDialogue(player, "I think I need a key of some sort...")
                return
            }

            if (inInventory(player, Items.TOBANS_GOLD_2393)) {
                sendMessage(player, "I don't need another one of these.")
                return
            }

            sendMessage(player, "You use the key Og gave you.")
            scenery?.let { replaceScenery(it, 2828, 3) }
            sendItemDialogue(player, Items.TOBANS_GOLD_2393, "You find a stash of gold inside.")

            addDialogueAction(player) { _, _ ->
                if (freeSlots(player) == 0) {
                    sendDialogue(player, "You don't have enough inventory space for that.")
                    return@addDialogueAction
                } else {
                    addItem(player, Items.TOBANS_GOLD_2393, 1)
                }
            }
        }
    }
}
