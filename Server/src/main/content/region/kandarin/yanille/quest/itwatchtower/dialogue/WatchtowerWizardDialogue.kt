package content.region.kandarin.yanille.quest.itwatchtower.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.IfTopic
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.*

/**
 * Represents the Wizard dialogue.
 *
 * # Relations
 * - [Watchtower Quest][content.region.kandarin.yanille.quest.itwatchtower.Watchtower]
 */
@Initializable
class WatchtowerWizardDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        val questStage = getQuestStage(player, Quests.WATCHTOWER)
        if (npc.id == NPCs.WIZARD_5195) {
            if (questStage == 75) {
                player(FaceAnim.HALF_ASKING, "Any chance of advice about this potion?")
                stage = 905
                return true
            }

            if (questStage == 85) {
                player(FaceAnim.HALF_ASKING, "Any chance of a hand?")
                stage = 947
                return true
            }

            if (questStage == 95) {
                player(FaceAnim.HALF_ASKING, "What should I do now?")
                stage = 1300
                return true
            }

            if (questStage == 100) {
                npc("All's well that ends well.")
                stage = END_DIALOGUE
                return true
            }

            player("What's going on here?")
            stage = 100
            return true
        }

        val itemIds = listOf(
            Items.RELIC_PART_1_2373,
            Items.RELIC_PART_2_2374,
            Items.RELIC_PART_3_2375
        )

        val attributes = listOf(
            GameAttributes.WATCHTOWER_RELIC_1,
            GameAttributes.WATCHTOWER_RELIC_2,
            GameAttributes.WATCHTOWER_RELIC_3
        )

        val hasAll = itemIds.all { inInventory(player, it) }
        val turnedInAny = attributes.any { getAttribute(player, it, false) }
        val turnedInAll = attributes.all { getAttribute(player, it, false) }

        if (hasAll && !turnedInAny) {
            player("An ogre gave me these...")
            for (i in itemIds.indices) {
                removeItem(player, itemIds[i])
                setAttribute(player, attributes[i], true)
            }
            stage = 600
            return true
        }

        var handedIn = false
        for (i in itemIds.indices) {
            if (!getAttribute(player, attributes[i], false) && inInventory(player, itemIds[i])) {
                removeItem(player, itemIds[i])
                setAttribute(player, attributes[i], true)
                handedIn = true
            }
        }

        if (handedIn || turnedInAll) {
            player("An ogre gave me this...")
            stage = if (handedIn) 700 else 600
            return true
        }

        val alreadyGive = itemIds.indices.any { i ->
            getAttribute(player, attributes[i], false) && inInventory(player, itemIds[i])
        }

        if (alreadyGive) {
            player("An ogre gave me this...")
            stage = 701
            return true
        }

        if (questStage in 10..20) {
            npc("Ah the warrior returns!")
            stage = 702
            return true
        }

        if (questStage == 60) {
            player("I have found the cave of ogre shaman, but I cannot", "touch them!")
            stage = 900
            return true
        }

        if (questStage == 75) {
            npc(FaceAnim.HALF_ASKING, "Any more news?")
            stage = 940
            return true
        }

        if (questStage == 85) {
            npc("Hello again. Did the potion work?")
            stage = 945
            return true
        }

        if (questStage == 90) {
            npc("Hello again. Did the potion work?")
            stage = 948
            return true
        }

        if (questStage == 95 && !getAttribute(player, GameAttributes.WATCHTOWER_SYSTEM_ACTIVATED, false)) {
            npc("The system is not activated yet. Throw the switch to start it.")
            stage = END_DIALOGUE
            return true
        }

        if (getAttribute(player, GameAttributes.WATCHTOWER_RIDDLE, false) ||
            inInventory(player, Items.SKAVID_MAP_2376)
        ) {
            npc("How is the quest going?")
            stage = 800
            return true
        }

        if (questStage == 100) {
            npcl(FaceAnim.HAPPY, "Greetings, friend. I trust all is well with you? Yanille is safe at last!")
            stage = 1100
            return true
        }

        if (questStage >= 1) {
            npc("Hello again. Did you find anything of interest?")
            stage = 30
        } else {
            npc("Who are you? Are you one of the new guards?")
        }

        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> player("No, I'm an adventurer.").also { stage++ }
            1 -> npc("Well what are you doing here?").also { stage++ }
            2 -> player("Looking for adventures - what else?").also { stage++ }
            3 -> npc("Oh my, oh my! What does it all matter in the end,", "anyway?").also { stage++ }
            4 -> options("What's the matter?", "You wizards are always complaining.").also { stage++ }
            5 -> when (buttonId) {
                1 -> player("What's the matter?").also { stage++ }
                2 -> player("You wizards are always complaining.").also { stage = 200 }
            }

            6 -> npc("Oh dear, oh dear. Darn and drat!").also { stage++ }
            7 -> npc("We try hard to keep this town protected.").also { stage++ }
            8 -> npc("But how can we do that when the Watchtower isn't", "working?").also { stage++ }
            9 -> player("What do you mean it isn't working?").also { stage++ }
            10 -> npc("The Watchtower here works by the power of magic. An", "ancient spell designed to ward off ogres that has been", "in place here for many moons.").also { stage++ }
            11 -> npc("The exact knowledge of the spell is lost to us now, but", "the essence of the spell has been infused into four", "powering crystals that keep the tower protected from", "the hordes in the Feldips.").also { stage++ }
            12 -> options("So how come the spell doesn't work?", "You wizards are always complaining.").also { stage++ }
            13 -> when (buttonId) {
                1 -> player("So how come the spell doesn't work?").also { stage++ }
                2 -> player("You wizards are always complaining.").also { stage = 200 }
            }

            14 -> npc("The crystals! The crystals have been taken!").also { stage++ }
            15 -> player("Taken?").also { stage++ }
            16 -> npc("Stolen!").also { stage++ }
            17 -> player("Stolen?").also { stage++ }
            18 -> npc("Yes, yes! Do I have to repeat myself?").also { stage++ }
            19 -> options("Can I be of help?", "I'm not sure I can help.", "I'm not interested.").also { stage++ }
            20 -> when (buttonId) {
                1 -> player("Can I be of help?").also { stage++ }
                2 -> player("I'm not sure I can help.").also { stage = 300 }
                3 -> player("I'm not interested.").also { stage = 400 }
            }
            21 -> npc("Help? Oh wonderful, dear traveller!").also { stage++ }
            22 -> npc("Yes I could do with an extra pair of eyes here.").also { stage++ }
            23 -> player("???").also { stage++ }
            24 -> npc("There must be some evidence of what has happened", "here. Perhaps you could assist me in searching for", "clues?").also { stage++ }
            25 -> player("I would be happy to.").also { stage++ }
            26 -> npc("Try searching the surrounding area. If you find", "anything unusual, bring it here.").also { stage++ }
            27 -> npc("Try the bushes - I've read enough adventure stories to", "know that clues get caught in bushes all the time.").also { stage++ }
            28 -> npc("I will tell the guards to let you past - that way, you", "can just use the ladder to get in and out.").also { stage++ }
            29 -> {
                end()
                setQuestStage(player, Quests.WATCHTOWER, 1)
            }

            30 -> {
                when {
                    allInInventory(player, *EVIDENCE_IDS) -> {
                        player("Have a look at these...")
                        stage = 34
                    }
                    anyInInventory(player, *EVIDENCE_IDS) -> {
                        EVIDENCE_IDS.forEach {
                            player("I found this ${it.asItem().name}...")
                            stage = 32
                        }
                    }
                    else -> {
                        player("No, sorry, nothing yet.")
                        stage = 31
                    }
                }
            }
            31 -> npc("Oh dear, oh dear, there must be something,", "somewhere...").also { stage = END_DIALOGUE }

            32 -> npc("Let me see...").also { stage++ }
            33 -> npc(FaceAnim.NEUTRAL, "No, sorry, this is not evidence. You need to keep", "searching, I'm afraid.").also { stage = END_DIALOGUE }

            34 -> npc("Interesting, very interesting.").also { stage++ }
            35 -> npc("Long nails...grey in colour...well chewed...").also { stage++ }
            36 -> npc("Of course! They belong to a skavid!").also { stage++ }
            37 -> player("A skavid?").also { stage++ }
            38 -> npc("A servant race to the ogres: grey, depressed-looking", "creatures, always losing nails, teeth and hair!").also { stage++ }
            39 -> npc("They inhabit the caves in the Feldip Hills.").also { stage++ }
            40 -> npc("They normally keep to themselves, though. It's unusual", "for them to venture from their caves.").also { stage++ }
            41 -> options("What do you suggest I do?", "Shall I search the caves?").also { stage++ }
            42 -> when (buttonId) {
                1 -> player("What do you suggest I do?").also { stage++ }
                2 -> player("Shall I search the caves?").also { stage++ }
            }
            43 -> npc("It's no good searching the caves. Well, not yet, anyway.").also { stage++ }
            44 -> player("Why not?").also { stage++ }
            45 -> npc("They are deep and complex. The only way you will", "navigate the caves is to have a map or something.").also { stage++ }
            46 -> npc("It may be that the ogres have one...").also { stage++ }
            47 -> player("And how do you know that?").also { stage++ }
            48 -> npc("Well... I don't.").also { stage++ }
            49 -> options("So what do I do?", "I won't bother then.").also { stage++ }
            50 -> when (buttonId) {
                1 -> player("So what do I do?").also { stage++ }
                2 -> player("I won't bother then.").also { stage = 500 }
            }
            51 -> npc("You need to be fearless and gain entrance to", "Gu'Tanoth, the city of the ogres, then find out how to", "navigate the caves.").also { stage++ }
            52 -> player("That sounds scary...").also { stage++ }
            53 -> npc("Ogres are nasty creatures, yes. Only a strong warrior,", "and a clever one at that, can get the better of them.").also { stage++ }
            54 -> player("What do I need to do to get into the city?").also { stage++ }
            55 -> {
                npc("Well, the guards need to be dealt with. You could start", "by checking out the ogre settlements found around", "here.",)
                removeItem(player, Items.FINGERNAILS_2384)
                stage++
            }
            56 -> npc("Tribal ogres often dislike their neighbours...").also { stage++ }
            57 -> npc("In the meantime, I'll throw those fingernails out for", "you.").also { stage++ }
            58 -> {
                end()
                setQuestStage(player, Quests.WATCHTOWER, 2)
            }

            100 -> npc("You'll have to speak with the Watchtower Wizard", "about that.").also { stage = END_DIALOGUE }
            200 -> npc("Complaining? Complaining!").also { stage++ }
            201 -> npcl(FaceAnim.ANNOYED, "What folks these days don't realise is that if it weren't for us wizards, this entire world would be overrun with every creature you could possibly imagine. And some you couldn't even conceive of!").also { stage = END_DIALOGUE }
            300 -> npcl(FaceAnim.NEUTRAL, "That's typical, nowadays. It's left to us wizards to do all the work.").also { stage = END_DIALOGUE }
            400 -> npc("Hmph! Suit yourself.").also { stage = END_DIALOGUE }
            500 -> npcl(FaceAnim.NEUTRAL, "Won't bother? Won't bother! Perhaps this quest is too hard for you?").also { stage = END_DIALOGUE }

            600 -> npc(FaceAnim.HAPPY, "Excellent! That seems to be all the pieces. Now I can", "assemble it...").also {
                playAudio(player, Sounds.WATCHTOWER_MAKE_RELIC_3267, 1)
                animate(findLocalNPC(player, npc.id)!!, Animations.CLIMB_JUMP_UP_5352)
                stage++
            }
            601 -> {
                npc("Hmm, yes, it is as I thought... A statue symbolising an", "ogre warrior of old.").also {
                    stage++
                }
            }
            602 -> npc(FaceAnim.HAPPY, "Well, if you ever want to make friends with an ogre,", "this is the item to have!").also {
                end()
                sendItemDialogue(player, Items.OGRE_RELIC_2372, "The wizard gives you a complete statue.")
                setQuestStage(player, Quests.WATCHTOWER, 10)
                addItemOrDrop(player, Items.OGRE_RELIC_2372, 1)
                removeAttributes(player,
                    GameAttributes.WATCHTOWER_TOBAN_GOLD,
                    GameAttributes.WATCHTOWER_TOBAN_KEY,
                    GameAttributes.WATCHTOWER_GORAD_TOOTH,
                    GameAttributes.WATCHTOWER_DRAGON_BONES,
                    GameAttributes.WATCHTOWER_RELIC_3,
                    GameAttributes.WATCHTOWER_RELIC_2,
                    GameAttributes.WATCHTOWER_RELIC_1,
                )
            }

            700 -> npcl(FaceAnim.HAPPY, "Ah, it's part of an old ogre statue. I'll see if I can fix it up for you. It might come in handy. There may be more parts to find... I'll keep this for later.").also { stage = END_DIALOGUE }
            701 -> npc("I already have that part...").also { stage = END_DIALOGUE }

            702 -> npc("Have you found a way into Gu'Tanoth yet?").also { stage++ }
            703 -> player("I can't get past the guards.").also { stage++ }
            704 -> npc("Well, ogres dislike others apart from their kind", "What you need is some form of proof of friendship.", "Something to trick them into believing you are their friend").also { stage++ }
            705 -> {
                val hasRelic = hasAnItem(player, Items.OGRE_RELIC_2372).container != null
                if(!hasRelic) {
                    options("I have lost the relic you gave me.", "I will find my way in, no problem.").also { stage++ }
                } else {
                    npc("...Which shouldn't be too hard considering their intelligence!").also { stage = END_DIALOGUE }
                }
            }
            706 -> options("I have lost the relic you gave me.", "I will find my way in, no problem.").also { stage++ }
            707 -> when(buttonId) {
                1 -> player("I have lost the relic you gave me.").also { stage++ }
                2 -> player("I will find my way in, no problem.").also { stage = 710 }
            }
            708 -> npc("What! lost the relic ? How careless!", "It's a good job I copied that design then...").also { stage++ }
            709 -> {
                end()
                if (freeSlots(player) == 0) {
                    sendMessage(player, "You don't have enough inventory space.")
                    return true
                }
                npc("You can take this copy instead, its just as good.")
                addItem(player, Items.OGRE_RELIC_2372)
                stage = END_DIALOGUE
            }
            710 -> npc("Yes, I'm sure you will...good luck.").also { stage = END_DIALOGUE }

            800 -> playerl(FaceAnim.NEUTRAL,"I have worked out the guard's puzzle.").also { stage++ }
            801 -> npc("My, my. A wordsmith as well as a hero.").also { stage++ }
            802 -> options("I am still trying to navigate the skavid caves.", "I am trying to get into the shamans' cave.", "It is going well.").also { stage++ }
            803 -> when(buttonId) {
                1 -> playerl(FaceAnim.NEUTRAL, "I am still trying to navigate the skavid caves.").also { stage++ }
                2 -> playerl(FaceAnim.NEUTRAL,"I am trying to get into the shamans' cave.").also { stage = 805 }
                3 -> playerl(FaceAnim.NEUTRAL,"It is going well.").also { stage = 815 }
            }
            804 -> npcl(FaceAnim.NEUTRAL, "Take some illumination with you or else it will be dark.").also { stage = END_DIALOGUE }
            805 -> npcl(FaceAnim.THINKING, "Yes, it will be well guarded. Hmmm, let me see...").also { stage++ }
            806 -> npcl(FaceAnim.FRIENDLY, "Ah yes, I gather some ogres are allergic to certain herbs. Now what was it? It had white berries and blue leaves...").also { stage++ }
            807 -> npcl(FaceAnim.HAPPY, "Cave nightshade, that's it! You should try looking through some of the caves.").also { stage++ }
            808 -> player(FaceAnim.HAPPY, "Alright!").also { stage++ }
            809 -> npcl(FaceAnim.HAPPY, "And don't forget to look out for the dragons!").also { stage++ }
            810 -> player(FaceAnim.PANICKED, "The WHAT?").also { stage++ }
            811 -> npc("Dragons.").also { stage++ }
            812 -> npcl(FaceAnim.FRIENDLY, "Didn't you know that the ogres keep blue dragons as pets and for magical ingredients?").also { stage++ }
            813 -> npcl(FaceAnim.FRIENDLY, "Well, regardless, I'm sure a bold, brave adventurer such as yourself won't have any problems.").also { stage++ }
            814 -> player(FaceAnim.THINKING, "Great...").also { stage = END_DIALOGUE }
            815 -> npcl(FaceAnim.FRIENDLY, "That's good to hear. We are much closer to fixing the tower now.").also { stage = END_DIALOGUE }

            900 -> npc(FaceAnim.NEUTRAL, "That is because of their magical powers. We must fight", "them with their own methods; do not speak to them! I", "suggest a potion...").also { stage++ }
            901 -> npc(FaceAnim.NEUTRAL, "Collect a guam leaf, add some jangerberries, then mix in", "some ground bat bones.").also { stage++ }
            902 -> npc(FaceAnim.NEUTRAL, "It is essential to return it to me before you use it, so", "that I can empower it with my magic.").also { stage++ }
            903 -> npc(FaceAnim.NEUTRAL, "Be very careful how you mix it as it is extremely", "volatile. Mixing ingredients of this type in the wrong", "order can cause explosions!").also { stage++ }
            904 -> npc(FaceAnim.NEUTRAL, "I hope you've been brushing up Herblore and Magic? I", "must warn you, only experienced magicians can use the", "potion. It is too dangerous in the hands of the unskilled.").also {
                setQuestStage(player, Quests.WATCHTOWER, 75)
                stage = END_DIALOGUE
            }

            905 -> npc(FaceAnim.WORRIED, "Nope. Try the Watchtower Wizard.").also { stage = END_DIALOGUE }
            940 -> {
                if(removeItem(player, Items.POTION_2394)) {
                    addItem(player, Items.MAGIC_OGRE_POTION_2395)
                    player("I have made the potion.")
                    stage = 941
                } else {
                    playerl(FaceAnim.HALF_ASKING, "Can you tell me again what I need for the potion?")
                    stage = 950
                }
            }
            941 -> npc(FaceAnim.FRIENDLY, "That's great news; let me infuse it with magic...").also { stage++ }
            942 -> {
                lock(player, 2)
                sendMessage(player, "The wizard mumbles strange words over the liquid...")
                runTask(player, 2) {
                    npc(FaceAnim.NEUTRAL, "Here it is - a dangerous substance. I must remind you", "that this potion can only be used if your Magic level is", "high enough.")
                    stage = 943
                }
            }

            943 -> player(FaceAnim.ASKING, "How high is high enough?").also { stage++ }
            944 -> npc(FaceAnim.NEUTRAL, "Well, if you can cast Bones to Bananas, or any spells", "beyond that, you should be alright.").also {
                if(!getAttribute(player, GameAttributes.WATCHTOWER_SHAMAN_SPAWN, false)) {
                    spawnShamanNPC(player)
                }
                stage = END_DIALOGUE
            }

            945 -> playerl(FaceAnim.HALF_GUILTY, "I am still working to rid us of the shamans...").also { stage++ }
            946 -> npc(FaceAnim.FRIENDLY,"May you have success in your task.").also { stage = END_DIALOGUE }
            947 -> npcl(FaceAnim.FRIENDLY,"Well, you could ask the Watchtower Wizard for one.").also { stage = END_DIALOGUE }

            948 -> player(FaceAnim.HAPPY,"Indeed it did! I wiped out those ogre shamans!").also { stage++ }
            949 -> npc(FaceAnim.HAPPY, "Magnificent! At last you've brought all the crystals.").also { stage = 1200 }

            950 -> npcl(FaceAnim.FRIENDLY, "Yes indeed, let me see, what was it again...").also { stage++ }
            951 -> {
                if (inInventory(player, Items.CLEAN_GUAM_249)) {
                    npcl(FaceAnim.FRIENDLY,"You need a guam leaf.").also { stage = 960 }
                } else {
                    npcl(FaceAnim.FRIENDLY,"You will also need a guam leaf. That goes into the mix first.").also { stage = 970 }
                }
            }
            960 -> playerl(FaceAnim.HALF_GUILTY, "I have one right here.").also { stage++ }
            961 -> npcl(FaceAnim.FRIENDLY,"Wonderful! You will need to add that first.").also { stage = 980 }
            970 -> playerl(FaceAnim.HALF_GUILTY, "What is the final ingredient?").also { stage = 980 }
            980 -> {
                if (inInventory(player, Items.JANGERBERRIES_247)) {
                    npcl(FaceAnim.FRIENDLY,"You need some jangerberries.").also { stage = 990 }
                } else {
                    npcl(FaceAnim.FRIENDLY,"You will also need some jangerberries. Those go into the mix before the final ingredient.").also { stage = 1000 }
                }
            }
            990 -> playerl(FaceAnim.HALF_GUILTY, "I have some of those.").also { stage++ }
            991 -> npcl(FaceAnim.FRIENDLY,"Wonderful! You will need to add those after the guam leaf has gone in, but before the other ingredient.").also { stage = 1010 }
            1000 -> playerl(FaceAnim.HALF_GUILTY, "What is the final ingredient?").also { stage = 1010 }
            1010 -> npcl(FaceAnim.FRIENDLY,"The final ingredient is ground bat bones.").also { stage++ }
            1011 -> {
                when {
                    inInventory(player, Items.GROUND_BAT_BONES_2391) -> {
                        playerl(FaceAnim.HALF_GUILTY, "Are these ground up well enough?").also { stage = 1020 }
                    }
                    inInventory(player, Items.BAT_BONES_530) -> {
                        playerl(FaceAnim.HALF_GUILTY, "I have some bat bones; how can I grind them?").also { stage = 1030 }
                    }
                    else -> {
                        npcl(FaceAnim.FRIENDLY,"You will need to find some bat bones and grind them using a pestle and mortar.").also { stage = END_DIALOGUE }
                    }
                }
            }
            1020 -> npcl(FaceAnim.FRIENDLY,"Yes, those will do nicely.").also { stage++ }
            1021 -> npcl(FaceAnim.FRIENDLY,"When you have mixed the potion correctly, then I can empower the potion with magic and the ogre shamans can be destroyed.").also { stage = END_DIALOGUE }
            1030 -> npcl(FaceAnim.FRIENDLY,"A pestle and mortar will more than do the trick.").also { stage++ }
            1031 -> npcl(FaceAnim.FRIENDLY,"When you have mixed the potion correctly, then I can empower the potion with magic and the ogre shamans can be destroyed.").also { stage = END_DIALOGUE }

            1100 -> showTopics(
                IfTopic("Do you have any more quests for me?", 1101, getAttribute(player, GameAttributes.WATCHTOWER_TELEPORT, false), false),
                IfTopic("I lost the scroll you gave me.", 1002, !getAttribute(player, GameAttributes.WATCHTOWER_TELEPORT, false), false),
                Topic("That's okay.", 1103, false)
            )
            1101 -> npcl(FaceAnim.HALF_THINKING, "More quests? No, indeed, adventurer. You have done us a great service, already.").also { stage = END_DIALOGUE }
            1102 -> if(inInventory(player, Items.SPELL_SCROLL_2396)) {
                npcl(FaceAnim.LAUGH, "Ho, ho, ho! A comedian to the finish. There it is, in your backpack!")
                stage = END_DIALOGUE
            } else {
                end()
                npcl(FaceAnim.THINKING, "Never mind, have another...")
                addItem(player, Items.SPELL_SCROLL_2396, 1)
            }
            1103 -> npcl(FaceAnim.FRIENDLY, "We are always in your debt; do come and visit us again.").also { stage = END_DIALOGUE }

            1200 -> npc(FaceAnim.NEUTRAL, "Now the shield generator can be activated and, once", "again, Yanille will be safe from the threat of the ogres.").also { stage++ }
            1201 -> npc(FaceAnim.NEUTRAL, "Put the crystals on the pillars there and throw the lever", "to activate the system.").also {
                setQuestStage(player, Quests.WATCHTOWER, 95)
                stage = END_DIALOGUE
            }

            1300 -> npcl(FaceAnim.NEUTRAL, "You will need to speak to the Watchtower Wizard. Seriously, he's the one in charge here.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.WATCHTOWER_WIZARD_872, NPCs.WIZARD_5195)

    companion object {
        private val EVIDENCE_IDS = intArrayOf(
            Items.FINGERNAILS_2384,
            Items.DAMAGED_DAGGER_2387,
            Items.TATTERED_EYE_PATCH_2388,
            Items.OLD_ROBE_2385,
            Items.UNUSUAL_ARMOUR_2386,
        )

        @JvmStatic
        private fun spawnShamanNPC(player: Player) {
            val shamanNPCIds = arrayOf(5183, 5180, 5175, 5186, 5192, 5189)
            val locations = arrayOf(
                Location.create(2592, 9436, 0),
                Location.create(2582, 9437, 0),
                Location.create(2577, 9451, 0),
                Location.create(2599, 9461, 0),
                Location.create(2607, 9451, 0),
                Location.create(2606, 9438, 0)
            )

            val spawnedNpcs = mutableListOf<NPC>()

            for (i in shamanNPCIds.indices) {
                val npc = NPC.create(shamanNPCIds[i], locations[i])
                npc.init()
                spawnedNpcs.add(npc)
            }

            setAttribute(player, GameAttributes.WATCHTOWER_SHAMAN_SPAWN, true)
            setAttribute(player, GameAttributes.WATCHTOWER_OGRE_DESTROY_COUNT, 0)

            registerLogoutListener(player, "shaman_spawn") {
                for (npc in spawnedNpcs) {
                    npc.clear()
                }
            }
        }
    }
}
