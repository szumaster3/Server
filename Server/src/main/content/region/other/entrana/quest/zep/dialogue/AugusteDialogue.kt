package content.region.other.entrana.quest.zep.dialogue

import content.data.items.DyeItem
import content.region.other.entrana.quest.zep.cutscene.ExperimentCutscene
import core.api.*
import core.game.dialogue.*
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.GameWorld
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.*

@Initializable
class AugusteDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(componentID: Int, buttonID: Int): Boolean {
        val hasPapyrus = inInventory(player!!, Items.PAPYRUS_970, 3)
        val hasCandle = inInventory(player!!, Items.CANDLE_36, 1)
        val hasWool = inInventory(player!!, Items.BALL_OF_WOOL_1759, 1)
        val hasPotatoes = inInventory(player!!, Items.POTATOES10_5438, 1)

        val hasDyedBalloon = DyeItem.values().map { it.origamiBallonId }.toIntArray()
        val hasPlain = inInventory(player, Items.ORIGAMI_BALLOON_9934)
        val hasDyed = anyInInventory(player, *hasDyedBalloon)

        val hasSandbags = inInventory(player!!, Items.SANDBAG_9943, 8)
        val hasSilk = inInventory(player!!, Items.SILK_950, 10)
        val hasBowl = inInventory(player!!, Items.UNFIRED_BOWL_1791, 1)
        val hasSapling = inInventory(player, Items.AUGUSTES_SAPLING_9932) && inBank(player, Items.AUGUSTES_SAPLING_9932)
        val hasGogglesAndCap = inInventory(player, Items.BOMBER_CAP_9945, Items.GNOME_GOGGLES_9472)

        when (getQuestStage(player, Quests.ENLIGHTENED_JOURNEY)) {
            0 -> when (stage) {
                0 -> npc(FaceAnim.HAPPY, "Greetings! would you like to be my number one", "accomplice?", "I mean, assistant?").also { stage++ }
                1 -> player(FaceAnim.ASKING, "Who are you?").also { stage++ }
                2 -> npc(FaceAnim.HAPPY, "I am Auguste. I am going to be the first balloonist in", "all of " + GameWorld.settings!!.name + "!").also { stage++ }
                3 -> player(FaceAnim.HALF_THINKING, "Balloo-what? I thought only monks lived on Entrana.").also { stage++ }
                4 -> npc(FaceAnim.SUSPICIOUS,"Well, they do...I was a monk. But I'm so sick of living", "on this island! I want to explore new frontiers!").also { stage++ }
                5 -> player(FaceAnim.LAUGH,"And go where no man has gone before?").also { stage++ }
                6 -> npc(FaceAnim.HAPPY, "Maybe! That sounds good. I was thinking just over to", "Taverley, though.").also { stage++ }
                7 -> player(FaceAnim.ASKING,"Why not take the boat then?").also { stage++ }
                8 -> npc(FaceAnim.ANGRY, "Ugh! Ocean! Don't talk to me about the ocean...oh", "dear... I feel ill.").also { stage++ }
                9 -> player(FaceAnim.SCARED,"Whoa, ok, no ocean. How exactly are you going to get", "off the island, then?").also { stage++ }
                10 -> npc(FaceAnim.NEUTRAL,"I have devised a new way to travel. But I need an", "assistant balloonist to help me build my design.").also { stage++ }
                11 -> npc(FaceAnim.HALF_ASKING, "Are you an experienced adventurer willing to help me?").also { stage++ }
                12 -> options("Yes! Sign me up.", "Not right now.").also { stage++ }
                13 -> when (buttonID) {
                    1 -> if (hasLevelStat(player, Skills.CRAFTING, 36) && hasLevelStat(player, Skills.FARMING, 30) && hasLevelStat(player, Skills.FIREMAKING, 20) && getQuestPoints(player) >= 21) {
                        player(FaceAnim.FRIENDLY, "Yes! Sign me up.").also { stage++ }
                    } else {
                        end()
                        sendMessage(player, RED + "You do not meet the requirements to start: 'Enlightened Journey'.")
                        stage = END_DIALOGUE
                    }

                    2 -> player("Not right now.").also { stage = END_DIALOGUE }
                }

                14 -> npc(FaceAnim.HAPPY, "Wonderful! Let's get started.").also { stage++ }
                15 -> player(FaceAnim.HALF_THINKING,"Wait. I still don't know exactly what we're doing.").also { stage++ }
                16 -> npc(FaceAnim.SCARED, "Of course, of course, how foolish of me.", "Well, we are going to make a balloon.").also { stage++ }
                17 -> player(FaceAnim.HALF_THINKING,"Which is what exactly?").also { stage++ }
                18 -> npc(FaceAnim.HAPPY, "Let me show you!").also { stage++ }
                19 -> {
                    end()
                    setVarbit(player, Vars.VARBIT_QUEST_ENLIGHTENED_JOURNEY_PROGRESS_2866, 1, true)
                    setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 1).also { stage++ }
                    openInterface(player, Components.HOT_AIR_BALLOON_DIAGRAM_472)
                }
            }

            1 -> when (stage) {
                0 -> playerl(FaceAnim.FRIENDLY, "How exactly do you plan to get to Taverley in that? How will it work?").also { stage++ }
                1 -> npcl(FaceAnim.FRIENDLY, "Have you noticed how ashes float above fires for long periods of time?").also { stage++ }
                2 -> options("Umm, yes. What's your point?", "Err, no. I can't see that kind of detail.").also { stage++ }
                3 -> when (buttonID) {
                    1 -> playerl(FaceAnim.HALF_THINKING, "Umm, yes. What's your point?").also { stage = 5 }
                    2 -> playerl(FaceAnim.FRIENDLY, "Err, no. I can't see that kind of detail.").also { stage = 4 }
                }
                4 -> npcl(FaceAnim.FRIENDLY, "You don't? Oh...well...it's just, they rise, you know, from the fire... Maybe you should pay more attention to things!").also { stage++ }
                5 -> npcl(FaceAnim.HAPPY, "Don't you see? It's the hot air! It rises, taking the ashes with it.").also { stage++ }
                6 -> npc(FaceAnim.HAPPY, "I had this epiphany while I was at the glass blower's", "house. If we pump hot air into an envelope it will rise", "because it is lighter than the cold air around it.").also { stage++ }
                7 -> playerl(FaceAnim.HALF_THINKING, "Come again?").also { stage++ }
                8 -> npc(FaceAnim.NEUTRAL, "We are going to sew a big sack and light a fire under", "it. Once the sack fills with hot air it will begin to rise,", "taking us along with it in the basket.").also { stage++ }
                9 -> player(FaceAnim.PANICKED, "You seem pretty confident about this. Have you tested", "it?").also { stage++ }
                10 -> npc(FaceAnim.SAD, "Well, no. You see, I don't have the materials here to", "make any test balloons.").also { stage++ }
                11 -> npcl(FaceAnim.HAPPY, "That's where you come in!").also { stage++ }
                12 -> npc(FaceAnim.HAPPY, "You are going to collect materials for two test runs of", "the balloon.").also { stage++ }
                13 -> npc(FaceAnim.HAPPY, "You will need to get three sheets of papyrus, one ball of", "wool, one full sack of potatoes, and one unlit candle.").also { stage++ }
                14 -> playerl(FaceAnim.HALF_THINKING, "What am I supposed to do with all that junk?").also { stage++ }
                15 -> npc(FaceAnim.NEUTRAL, "When you have all of it, bring it to me and I'll explain", "what to do next. Any questions?").also { stage++ }
                16 -> playerl(FaceAnim.CALM_TALK, "Where do I get all this stuff?").also { stage++ }
                17 -> npc(FaceAnim.ANGRY, "You're the adventurer, you should know! Think of", "logical places, like, churches; they have tons of candles.").also { stage++ }
                18 -> {
                    end()
                    setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 2)
                }
            }

            2 -> when (stage) {
                0 -> npcl(FaceAnim.FRIENDLY, "Have you gotten the materials?").also { stage++ }
                1 -> options("Yes.", "No.").also { stage++ }
                2 -> when (buttonID) {
                    1 -> {
                        if (hasCandle && hasPapyrus && hasWool && hasPotatoes) {
                            playerl(FaceAnim.FRIENDLY, "Yes.").also { stage = 4 }
                        } else {
                            playerl(FaceAnim.FRIENDLY, "Yes.").also { stage = 10 }
                        }
                    }
                    2 -> playerl(FaceAnim.FRIENDLY, "No.").also { stage = 3 }
                }

                3 -> npcl(FaceAnim.FRIENDLY, "Failure is not an option.").also { stage = END_DIALOGUE }
                4 -> npcl(FaceAnim.HAPPY, "Good, you have everything! Now, I need you to create an origami balloon.").also { stage = 5 }
                5 -> playerl(FaceAnim.HALF_THINKING, "How do you make the origami balloon?").also { stage++ }
                6 -> npc(FaceAnim.HAPPY, "First, use the papyrus on the ball of wool. The papyrus", "is folded into an origami box and the yarn will support", "the heat source.").also { stage++ }
                7 -> npc(FaceAnim.HAPPY, "Next, add the unlit candle to the balloon structure. It", "will act as the heat source.").also { stage++ }
                8 -> npc(FaceAnim.HAPPY, "Once you have done that let me know and we will", "begin our experiment.").also { stage++ }
                9 -> {
                    end()
                    setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 3)
                }
                10 -> {
                    val missing = buildList {
                        if (!hasCandle)   add("an unlit candle")
                        if (!hasWool)     add("a ball of wool")
                        if (!hasPapyrus)  add("three sheets of papyrus")
                        if (!hasPotatoes) add("a full sack of potatoes")
                    }

                    val message = when (missing.size) {
                        0 -> "You have all the materials!"
                        1 -> "You need ${missing[0]}."
                        else -> "You need " + missing.dropLast(1).joinToString(", ") + ", and ${missing.last()}."
                    }

                    npcl(FaceAnim.FRIENDLY, message).also { stage = END_DIALOGUE }
                }
            }

            3 -> when (stage) {
                0 -> when {
                    hasDyed && !hasPlain -> npcl(FaceAnim.FRIENDLY, "I just need a plain origami balloon. The dye could contaminate the experiment.").also { stage = END_DIALOGUE }
                    hasPlain -> playerl(FaceAnim.HAPPY, "I finished the origami balloon!").also { stage = 4 }
                    else -> playerl(FaceAnim.FRIENDLY, "How do you make the origami balloon?").also { stage++ }
                }
                1 -> npcl(FaceAnim.FRIENDLY, "First, use the papyrus on the ball of wool. The papyrus is folded into an origami box and the yarn will support the heat source.").also { stage++ }
                2 -> npcl(FaceAnim.FRIENDLY, "Next, add the unlit candle to the balloon structure. It will act as the heat source.").also { stage++ }
                3 -> npcl(FaceAnim.FRIENDLY, "Once you have done that let me know and we will begin our experiment.").also { stage = END_DIALOGUE }
                4 -> npcl(FaceAnim.HAPPY, "Wonderful! I'll take that, and we'll conduct our first experiment.").also { stage++ }
                5 -> {
                    end()
                    if(removeItem(player, Items.ORIGAMI_BALLOON_9934)) {
                        ExperimentCutscene(player).start(true)
                        setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 4)
                    }
                }
            }

            4 -> when (stage) {
                0 -> npcl(FaceAnim.HALF_ASKING, "Do you have the other two sheets of papyrus and a full sack of potatoes?").also { stage++ }
                1 -> options("Yes, I have them here.", "Oh, I've misplaced them.").also { stage++ }
                2 -> when (buttonID) {
                    1 -> playerl(FaceAnim.HAPPY, "Yes, I have them here.").also {
                        val missing = buildList {
                            if (!hasPotatoes) add("a full sack of potatoes")
                            if (!inInventory(player!!, Items.PAPYRUS_970, 2)) add("more papyrus")
                        }
                        if (missing.isEmpty()) {
                            stage = 3
                        } else {
                            val message = if (missing.size == 1) "You need ${missing[0]}." else "You need ${missing.joinToString(" and ")}."
                            npcl(FaceAnim.FRIENDLY, message)
                            stage = END_DIALOGUE
                        }
                    }
                    2 -> playerl(FaceAnim.NEUTRAL, "Oh, I've misplaced them.").also { stage = END_DIALOGUE }
                }
                3 -> npc(FaceAnim.HAPPY, "Commendable. If I may have those, I will construct this", "experiment.").also { stage++ }
                4 -> {
                    end()
                    if(removeItem(player, Item(Items.PAPYRUS_970, 2)) && removeItem(player, Item(Items.POTATOES10_5438, 1))) {
                        ExperimentCutscene(player).start(true)
                        setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 5)
                    }
                }

            }

            5 -> when (stage) {
                0  -> playerl(FaceAnim.HALF_THINKING, "Those peasants... where did they come from?").also { stage++ }
                1  -> npc(FaceAnim.NEUTRAL, "Ahh, the flash mob phenomenon. Many have", "hypothesized that they are beings of great power sent to", "smite those who question the gods.").also { stage++ }
                2  -> playerl(FaceAnim.HALF_THINKING, " And this isn't worrying because...?").also { stage++ }
                3  -> npcl(FaceAnim.HAPPY, "Don't worry! I know exactly what I'm doing.").also { stage++ }
                4  -> npcl(FaceAnim.HAPPY, "Those experiments went extraordinarily well.").also { stage++ }
                5  -> player(FaceAnim.DISGUSTED_HEAD_SHAKE, "Was I the ONLY one who saw them burning?", "BURNING???").also { stage++ }
                6  -> npc(FaceAnim.HAPPY, "Yes, very well indeed. Now we will start building the", "balloon that will carry us off the island.").also { stage++ }
                7  -> npc(FaceAnim.FRIENDLY, "This task will be much greater than the last two. I hope", "you are prepared for it.").also { stage++ }
                8  -> playerl(FaceAnim.NEUTRAL, "This is madness.").also { stage++ }
                9  -> npcl(FaceAnim.HAPPY, "You need to get the following items:").also { stage++ }
                10 -> npc(FaceAnim.FRIENDLY, "Yellow dye","Red dye","Ten pieces of silk","A clay bowl").also { stage++ }
                11 -> npcl(FaceAnim.FRIENDLY, "and eight sandbags.").also { stage++ }
                12 -> npc(FaceAnim.FRIENDLY, "Sandbags can be made by getting empty sacks and", "filling them at the sand pit here on Entrana.").also { stage++ }
                13 -> npc(FaceAnim.FRIENDLY, "However, there are other sand pits around the world", "that will work as well; there is one in Yanille, Rellekka,", "and Zanaris.").also { stage++ }
                14 -> npc(FaceAnim.NEUTRAL, "You can bring items back to me as you get them, while", "you are waiting for the tree to grow.").also { stage++ }
                15 -> playerl(FaceAnim.HALF_THINKING, "What tree?").also { stage++ }
                16 -> npc(FaceAnim.NEUTRAL, "I am going to give you a willow sapling and a basket of", "apples. You must plant the willow sapling at a tree", "patch.").also { stage++ }
                17 -> npc(FaceAnim.NEUTRAL, "If you give the basket of apples to the gardener near", "the patch, he will look after the tree for you while it", "grows.").also { stage++ }
                18 -> npc(FaceAnim.NEUTRAL, "Don't lose the sapling! It took me a long time to save", "up enough for one. If you do, you'll have to pay me", "30,000 coins for a new one.").also { stage++ }
                19 -> npc(FaceAnim.CALM_TALK, "Once the tree is fully grown, cut twelve branches from", "it using secateurs. Bring the branches back here and", "use them on the metal frame on the platform to create","the basket.").also { stage++ }
                20 -> npc(FaceAnim.HAPPY, "Here you go. Now be very careful not to lose it!").also { stage++ }
                21 -> {
                    end()
                    if(freeSlots(player) < 2) {
                        npcl(FaceAnim.FRIENDLY, "Looks like you don't have enough room in your inventory for the basket and the sapling. Come back when you do.")
                        return true
                    }
                    setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 6)
                    addItemOrDrop(player, Items.AUGUSTES_SAPLING_9932, 1)
                    addItemOrDrop(player, Items.APPLES5_5386, 1)
                }
            }

            6 -> when (stage) {
                0 -> npcl(FaceAnim.FRIENDLY, "Do you have anything for me?").also { stage = 1 }
                1 -> options("Yes, I want to give you some items.", "I'm having trouble finding some of the items.", "I have lost my willow sapling. Can I buy a replacement?").also { stage = 2 }
                2 -> when (buttonID) {
                    1 -> playerl(FaceAnim.FRIENDLY, "Yes, I want to give you some items.").also { stage = 3 }
                    2 -> playerl(FaceAnim.FRIENDLY, "I'm having trouble finding some of the items.").also { stage = 12 }
                    3 -> playerl(FaceAnim.FRIENDLY, "I have lost my willow sapling. Can I buy a replacement?").also { stage = 20 }
                    else -> end()
                }
                3 ->
                    if(hasGiven(player, "ej-dye-red") &&
                        hasGiven(player, "ej-dye-yellow") &&
                        hasGiven(player, "ej-silk") &&
                        hasGiven(player, "ej-bowl") &&
                        hasGiven(player, "ej-sandbags"))
                    {
                        npcl(FaceAnim.FRIENDLY, "That's the last of it!")
                        setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 7)
                        removeAttributes(player, "ej-dye","ej-dye-red","ej-dye-yellow","ej-silk","ej-bowl","ej-sandbags")
                        stage = 28
                    }
                    else
                    {
                        showTopics(
                            IfTopic("Dye", 5, !hasGiven(player, "ej-dye"), true),
                            IfTopic("Sandbags", 12, !hasGiven(player, "ej-sandbags"), true),
                            IfTopic("Silk", 15, !hasGiven(player, "ej-silk"), true),
                            IfTopic("Bowl", 16, !hasGiven(player, "ej-bowl"), true),
                            Topic("Never mind.", END_DIALOGUE, true)
                        )
                    }
                5 -> {
                    val gaveYellow = hasGiven(player, "ej-dye-yellow")
                    val gaveRed = hasGiven(player, "ej-dye-red")
                    val hasYellow = inInventory(player, Items.YELLOW_DYE_1765, 1)
                    val hasRed = inInventory(player, Items.RED_DYE_1763, 1)
                    if (inInventory(player, Items.YELLOW_DYE_1766, 1) || inInventory(player, Items.RED_DYE_1764, 1)) {
                        npc(FaceAnim.SAD, "What am I supposed to do with a note?? I can't make", "a balloon from notes!")
                        stage = END_DIALOGUE
                    } else if (hasYellow && hasRed && !gaveYellow && !gaveRed) {
                        removeItem(player, Item(Items.YELLOW_DYE_1765, 1))
                        removeItem(player, Item(Items.RED_DYE_1763, 1))
                        setGiven(player, "ej-dye-yellow")
                        setGiven(player, "ej-dye-red")
                        setGiven(player, "ej-dye")
                        npcl(FaceAnim.FRIENDLY, "Ah, wonderful - the red and yellow dye. Thank you.")
                        stage = 3
                    } else if (hasYellow && !gaveYellow) {
                        removeItem(player, Item(Items.YELLOW_DYE_1765, 1))
                        setGiven(player, "ej-dye-yellow")
                        npcl(FaceAnim.FRIENDLY, "Ah, wonderful, yellow dye. Thank you.")
                        stage = if (hasGiven(player, "ej-red-dye")) 3 else 7
                    } else if (hasRed && !gaveRed) {
                        removeItem(player, Item(Items.RED_DYE_1763, 1))
                        setGiven(player, "ej-dye-red")
                        npcl(FaceAnim.FRIENDLY, "Red dye! Thank you.")
                        stage = if (hasGiven(player, "ej-yellow-dye")) 3 else 8
                    } else if (!hasYellow && !hasRed) {
                        when {
                            !gaveYellow && !gaveRed -> npcl(FaceAnim.FRIENDLY, "You don't have any dye with you.").also { stage = 9 }
                             gaveYellow && !gaveRed -> npcl(FaceAnim.FRIENDLY, "You don't have any dye with you.").also { stage = 10 }
                             !gaveYellow && gaveRed -> npcl(FaceAnim.FRIENDLY, "You don't have any dye with you.").also { stage = 11 }
                        }
                    }
                }
                6 -> {
                    npcl(FaceAnim.FRIENDLY, "That's the last of it!")
                    setQuestStage(player, Quests.ENLIGHTENED_JOURNEY, 7)
                    stage = 28
                }
                7 -> npcl(FaceAnim.FRIENDLY, "I still need red dye for the balloon.").also { stage = END_DIALOGUE }
                8 -> npcl(FaceAnim.FRIENDLY, "I still need yellow dye for the balloon.").also { stage = END_DIALOGUE }
                9 -> npcl(FaceAnim.FRIENDLY, "I need red and yellow dye for the balloon.").also { stage = END_DIALOGUE }
                10 -> npcl(FaceAnim.FRIENDLY, "I still need red dye for the balloon.").also { stage = END_DIALOGUE }
                11 -> npcl(FaceAnim.FRIENDLY, "I still need yellow dye for the balloon.").also { stage = END_DIALOGUE }
                12 -> {
                    val neededSandbags = 8
                    if (hasGiven(player, "ej-sandbags")) {
                        npcl(FaceAnim.FRIENDLY, "You have already given me sandbags.").also { stage = 3 }
                    } else if (hasSandbags) {
                        removeItem(player, Item(Items.SANDBAG_9943, neededSandbags))
                        setGiven(player, "ej-sandbags")
                        npc(FaceAnim.FRIENDLY, "Sandbags, thank you. This will allow us to change", "height.").also { stage = 3 }
                    } else {
                        npcl(FaceAnim.FRIENDLY, "You don't have enough sandbags. Please bring me eight.").also { stage = 13 }
                    }
                }
                13 -> npcl(FaceAnim.FRIENDLY, "Sandbags can be made by getting empty sacks and filling them at the sandpit here on Entrana.").also { stage++ }
                14 -> npcl(FaceAnim.FRIENDLY, "However, there are other sand pits around the world that will work as well.").also { stage = END_DIALOGUE }
                15 -> {
                    val neededSilk = 10
                    if (hasGiven(player, "ej-silk")) {
                        npcl(FaceAnim.FRIENDLY, "You have already given the silk.").also { stage = 3 }
                    } else if (inInventory(player, Items.SILK_951, neededSilk)) {
                        npc(FaceAnim.SAD, "What am I supposed to do with a note?? I can't make", "a balloon from notes!")
                        stage = END_DIALOGUE
                    } else if (hasSilk) {
                        removeItem(player, Item(Items.SILK_950, neededSilk))
                        setGiven(player, "ej-silk")
                        npcl(FaceAnim.FRIENDLY, "Silk for the balloon, thank you.").also { stage = 3 }
                    } else {
                        npcl(FaceAnim.FRIENDLY, "You don't have enough silk. Please bring me ten pieces.").also { stage = END_DIALOGUE }
                    }
                }
                16 -> {
                    if (hasGiven(player, "ej-bowl")) {
                        npcl(FaceAnim.FRIENDLY, "You have already given the bowl.").also { stage = 3 }
                    } else if (inInventory(player, Items.UNFIRED_BOWL_1792, 1)) {
                        npc(FaceAnim.SAD, "What am I supposed to do with a note?? I can't make", "a baloon from notes!")
                        stage = END_DIALOGUE
                    } else if (!hasBowl) {
                        npcl(FaceAnim.FRIENDLY, "I need a plain clay-fired bowl; they're quite easy to come by.").also { stage = END_DIALOGUE }
                    } else {
                        removeItem(player, Item(Items.UNFIRED_BOWL_1791, 1))
                        setGiven(player, "ej-bowl")
                        npcl(FaceAnim.FRIENDLY, "Ah the bowl. This will be used to hold the fuel while it heats the air in the balloon.").also { stage = 3 }
                    }
                }
                17 -> npcl(FaceAnim.FRIENDLY, "What do you need help with?").also { stage++ }
                18 -> showTopics(
                    Topic("Dye", 19, true),
                    Topic("Sandbags", 20, true),
                    Topic("Silk", 22, true),
                    Topic("Bowl", 23, true),
                    Topic("Never mind.", END_DIALOGUE, true)
                )
                19 -> npcl(FaceAnim.FRIENDLY, "I was told a while ago that there was a witch who made dye in Draynor Village. Maybe you should start by looking there.").also { stage = 18 }
                20 -> npcl(FaceAnim.FRIENDLY, "Sandbags can be made by getting empty sacks and filling them at the sandpit here on Entrana.").also { stage++ }
                21 -> npcl(FaceAnim.FRIENDLY, "However, there are other sand pits around the world that will work as well; there is one in Yanille, Rellekka, and Zanaris.").also { stage = 18 }
                22 -> npcl(FaceAnim.FRIENDLY, "Hmm, I believe silk is imported from the desert. Perhaps someone there can tell you where to find it.").also { stage = 18 }
                23 -> npcl(FaceAnim.FRIENDLY, "I think there is a spare one in the glass blower's house. I rent a room from him there, so I don't think he'll mind you taking it.").also { stage = 18 }
                24 -> npcl(FaceAnim.FRIENDLY, "It will cost you 30,000 gold coins to replace it, do you want to pay that?").also { stage++ }
                25 -> showTopics(
                    Topic("Yes.", 26),
                    Topic("No way!", END_DIALOGUE)
                )
                26 -> {
                    if (freeSlots(player) < 2) {
                        npcl(FaceAnim.FRIENDLY, "Looks like you don't have enough room in your inventory for the basket and the sapling. Come back when you do.")
                        return true
                    }
                    if (!inInventory(player, Items.COINS_995, 30000)) {
                        npcl(FaceAnim.FRIENDLY, "Looks like you don't have enough money. Come back when you do.")
                        return true
                    }
                    if (removeItem(player, Item(Items.COINS_995, 30000))) {
                        npcl(FaceAnim.FRIENDLY, "Here you go. Now be very careful not to lose it again!")
                        addItemOrDrop(player, Items.AUGUSTES_SAPLING_9932, 1)
                        stage = 27
                    }
                }
                27 -> npcl(FaceAnim.FRIENDLY, "Good luck with the balloon.").also { stage = END_DIALOGUE }
                28 -> npc(FaceAnim.HAPPY, "You just need to build the basket and I can finish the", "balloon! How are you getting on with the willow?").also { stage++ }
                29 -> showTopics(
                    Topic(FaceAnim.FRIENDLY, "I have lost my willow sapling. Can I buy a replacement?", 24),
                    Topic(FaceAnim.HALF_THINKING,"What do I do again?", 30),
                    Topic("Fine thanks.", END_DIALOGUE),
                )
                30 -> npc("Use the willow sapling I gave you to grow a willow tree.", "Cut twelve branches from it using secateurs. Use the", "branches on the platform here.").also { stage = END_DIALOGUE }
                else -> stage = END_DIALOGUE
            }

            7 -> when (stage) {
                0 -> npc(FaceAnim.HAPPY, "You just need to build the basket and I can finish the", "balloon! How are you getting on with the willow?").also { stage++ }
                1 -> options("I have lost my willow sapling. Can I buy a replacement?", "What do I do again?", "Fine thanks.").also { stage++ }
                2 -> when (buttonID) {
                    1 -> playerl(FaceAnim.FRIENDLY, "I have lost my willow sapling. Can I buy a replacement?").also { stage = 3 }
                    2 -> playerl(FaceAnim.FRIENDLY, "What do I do again?").also { stage = 7 }
                    3 -> playerl(FaceAnim.FRIENDLY, "Fine thanks.").also { stage = END_DIALOGUE }
                }
                3 -> npcl(FaceAnim.FRIENDLY, "It will cost you 30,000 gold coins to replace it, do you want to pay that?").also { stage++ }
                4 -> options("Yes.", "No way!").also { stage++ }
                5 -> when (buttonID) {
                    1 -> playerl(FaceAnim.FRIENDLY, "Yes.").also { stage++ }
                    2 -> playerl(FaceAnim.FRIENDLY, "No way!").also { stage = END_DIALOGUE }
                }
                6 -> if (!hasSapling && !removeItem(player, Item(Items.COINS_995, 30000))) {
                    npcl(FaceAnim.FRIENDLY, "Looks like you don't have enough money. Come back when you do.").also { stage = END_DIALOGUE }
                } else if (freeSlots(player) < 2) {
                    npcl(FaceAnim.FRIENDLY, "Looks like you don't have enough room in your inventory for the basket and the sapling. Come back when you do.").also { stage = END_DIALOGUE }
                } else if (!hasSapling && removeItem(player, Item(Items.COINS_995, 30000))) {
                    end()
                    npcl(FaceAnim.FRIENDLY, "Here you go. Now be very careful not to lose it again!")
                    addItemOrDrop(player, Items.AUGUSTES_SAPLING_9932, 1)
                    addItemOrDrop(player, Items.APPLES5_5386, 1)
                } else {
                    npcl(FaceAnim.FRIENDLY, "You already have one! Don't be greedy.").also { stage = END_DIALOGUE }
                }
                7 -> npcl(FaceAnim.FRIENDLY, "Use the willow sapling I gave you to grow a willow tree. Cut twelve branches from it using secateurs. Use the branches on the platform here.").also { stage = END_DIALOGUE }
            }

            8 -> when (stage) {
                0 -> npcl(FaceAnim.HAPPY, "Well, let's get going!").also { stage++ }
                1 -> options("Wait; tell me what we're doing.", "Okay.", "No, I'm not ready.").also { stage++ }
                2 -> when (buttonID) {
                    1 -> playerl(FaceAnim.FRIENDLY, "Wait; tell me what we're doing.").also { stage = 3 }
                    2 -> playerl(FaceAnim.HAPPY, "Okay.").also { stage = 23 }
                    3 -> playerl(FaceAnim.FRIENDLY, "No, I'm not ready.").also { stage = END_DIALOGUE }
                }

                3 -> npcl(FaceAnim.FRIENDLY, "Theoretically, we are ready for the maiden voyage.").also { stage++ }
                4 -> playerl(FaceAnim.FRIENDLY, "Theoretically?").also { stage++ }
                5 -> npcl(FaceAnim.FRIENDLY, "Well, of course. With you piloting I am sure we will survive.").also { stage++ }
                6 -> playerl(FaceAnim.FRIENDLY, "WHAT!? This is your balloon! Why aren't you going to pilot it? And what do mean 'survive'? You never said anything about me flying this bird!").also { stage++ }
                7 -> npcl(FaceAnim.FRIENDLY, "Don't be silly. I'm not going to pilot it. We will be safe in the basket, the only thing we will lose are the logs. You will pilot it, won't you?").also { stage++ }
                8 -> playerl(FaceAnim.FRIENDLY, "Fine, I will. So how do I control the balloon?").also { stage++ }
                9 -> npcl(FaceAnim.FRIENDLY, "Wonderful! Let me explain my hypothesis on how to control a balloon.").also { stage++ }
                10 -> playerl(FaceAnim.FRIENDLY, "Oh no, not another hypothesis!").also { stage++ }
                11 -> npcl(FaceAnim.FRIENDLY, "The balloon needs ten normal logs for fuel. I believe the balloon will be controlled based on weight, so you must not have more than 40kg with you.").also { stage++ }
                12 -> npcl(FaceAnim.FRIENDLY, "Also, I seem to have lost my tinderbox, do you think you could bring one? We need it to light the fire.").also { stage++ }
                13 -> playerl(FaceAnim.FRIENDLY, "And here, I thought all the legwork was done. Fine, I'll bring a tinderbox.").also { stage++ }
                14 -> npcl(FaceAnim.FRIENDLY, "Right! So, the balloon mechanics.").also { stage++ }
                15 -> npcl(FaceAnim.FRIENDLY, "Your prime direction will always be to land the balloon on the target at the end of our route. I've written to some friends in Taverley who have kindly painted one on the ground there.").also { stage++ }
                16 -> npcl(FaceAnim.FRIENDLY, "We must avoid everything, because the balloon is very fragile. Even clouds will be dangerous! But if my calculations are correct, we will be able to squeeze through some surprising spaces, if you pilot it well.").also { stage++ }
                17 -> npcl(FaceAnim.FRIENDLY, "Those sandbags you made earlier will give the balloon a big lift when dropped, whereas adding the logs will only make it rise a little bit. But don't use them up too quickly! We have no way of replenishing them.").also { stage++ }
                18 -> npcl(FaceAnim.FRIENDLY, "If you do run out of logs and sandbags you will not be able to go up any longer. Just make the best of it and hope we can make it to our destination without going upwards.").also { stage++ }
                19 -> npcl(FaceAnim.FRIENDLY, "I've added two ropes, the red one is an emergency rope, it will let the hot air out of the balloon and we will drop quickly. The other rope will only drop us a little.").also { stage++ }
                20 -> npcl(FaceAnim.FRIENDLY, "If we get into tribb...beg your pardon, trouble, in all likelihood we will crash. But do not fear! We should be fine. Just make sure you come back to Entrana so we can try again.").also { stage++ }
                21 -> npcl(FaceAnim.FRIENDLY, "If it all goes horribly wrong, you can always bail. If we're still over Entrana, we can land quickly and try again. However, once past the island, we will crash.").also { stage++ }
                22 -> npcl(FaceAnim.FRIENDLY, "Are you ready to go?").also { stage++ }
                23 -> {
                    end()
                    if(player.familiarManager.hasFamiliar() || player.familiarManager.hasPet()) {
                        npcl(FaceAnim.HALF_GUILTY, "You need to put your pet into your inventory before the flight.")
                        return true
                    }
                    if(player.settings.weight > 40) {
                        npcl(FaceAnim.HALF_GUILTY, "You have too much weight in your bag. You may only bring 40kg of weight.")
                        return true
                    }
                    if(!inInventory(player, Items.LOGS_1511, 10)) {
                        npcl(FaceAnim.HALF_GUILTY, "You don't have enough logs. You need ten normal logs.")
                        return true
                    }
                    if(!inInventory(player, Items.TINDERBOX_590, 1)) {
                        npcl(FaceAnim.HALF_GUILTY, "You need a tinderbox.")
                        return true
                    }
                    if(removeItem(player, Item(Items.LOGS_1511, 10))) {
                        lock(player, 3)
                        val musicID = Music.FLOATING_FREE_206
                        setMinimapState(player, 2)
                        openInterface(player, Components.ZEP_INTERFACE_470)
                        if(!player.musicPlayer.hasUnlocked(musicID))
                            player.musicPlayer.unlock(musicID)
                    }
                    stage = END_DIALOGUE
                }
            }

            100 -> when (stage) {
                0 -> npcl(FaceAnim.FRIENDLY, "Do you want to use the balloon? Just so you know, some locations require special logs and high Firemaking skills.").also { stage++ }
                1 -> options("Yes.", "No.", "Could you replace some items for me.").also { stage++ }
                2 -> {
                    val maxOption = if (hasGogglesAndCap) 4 else 3
                    when (buttonID.coerceIn(1..maxOption)) {
                        1 -> playerl(FaceAnim.FRIENDLY, "Yes.").also { stage = 3 }
                        2 -> playerl(FaceAnim.FRIENDLY, "No.").also { stage = END_DIALOGUE }
                        3 -> playerl(FaceAnim.HALF_ASKING, "Could you replace some items for me?").also { stage = 4 }
                        4 -> playerl(FaceAnim.HALF_ASKING, "Could you combine my cap and goggles?").also { stage = 7 }
                    }
                }
                3 -> {
                    end()
                    openInterface(player, Components.ZEP_BALLOON_MAP_469)
                    setComponentVisibility(player, Components.ZEP_BALLOON_MAP_469, 12, false)
                }

                4 -> {
                    if (hasAnItem(player, Items.BOMBER_CAP_9945).container != null) {
                        npcl(FaceAnim.NEUTRAL, "You still seem to have your cap.")
                    } else {
                        npcl(FaceAnim.FRIENDLY, "Here's your cap. Try to keep better track of it.")
                        addItemOrDrop(player, Items.BOMBER_CAP_9945)
                    }
                    stage++
                }

                5 -> {
                    if (hasAnItem(player, Items.BOMBER_JACKET_9944).container != null) {
                        npcl(FaceAnim.NEUTRAL, "You don't need a new jacket; you still have your original one.")
                    } else {
                        npcl(FaceAnim.FRIENDLY, "Here's your jacket, to keep you warm during flight.")
                        addItemOrDrop(player, Items.BOMBER_JACKET_9944)
                    }
                    stage++
                }

                6 -> {
                    end()
                    stage = END_DIALOGUE
                }

                7 -> {
                    end()
                    when {
                        removeItem(player, Items.BOMBER_CAP_9945) && removeItem(player, Items.GNOME_GOGGLES_9472) -> {
                            npcl(FaceAnim.FRIENDLY, "There you go! You look like a true airship pilot now.")
                            addItemOrDrop(player, Items.CAP_AND_GOGGLES_9946)
                        }
                        inEquipment(player, Items.BOMBER_CAP_9945) || inEquipment(player, Items.GNOME_GOGGLES_9472) -> {
                            npcl(FaceAnim.FRIENDLY, "I can't combine them if you are wearing them.")
                        }
                        freeSlots(player) < 2 -> {
                            npcl(FaceAnim.FRIENDLY, "You don't have enough free space for the cap and goggles.")
                        }
                    }
                }
            }
        }
        return true
    }

    private fun hasGiven(player: Player, key: String) = getAttribute(player, key, false)
    private fun setGiven(player: Player, key: String) = setAttribute(player, "/save:$key", true)

    override fun getIds(): IntArray = intArrayOf(NPCs.AUGUSTE_5049)
}

class AugusteFirstTalkAfterQuestDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        npc = NPC(NPCs.AUGUSTE_5049)
        when (stage) {
            0 -> playerl(FaceAnim.HALF_ASKING, "So what are you going to do now?").also { stage++ }
            1 -> npcl(FaceAnim.FRIENDLY, "I am considering starting a balloon enterprise. People all over ${GameWorld.settings?.name} will be able to travel in a new, exciting way.").also { stage++ }
            2 -> npcl(FaceAnim.HAPPY, "As my first assistant, you will always be welcome to use a balloon. You'll have to bring your own fuel, though.").also { stage++ }
            3 -> playerl(FaceAnim.HAPPY, "Thanks!").also { stage++ }
            4 -> npcl(FaceAnim.HAPPY, "I will base my operations in Entrana. If you'd like to travel to new places, come see me there.").also { stage++ }
            5 -> {
                end()
                finishQuest(player!!, Quests.ENLIGHTENED_JOURNEY)
            }
        }
    }
}
