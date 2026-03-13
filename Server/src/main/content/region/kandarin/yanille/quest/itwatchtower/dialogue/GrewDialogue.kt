package content.region.kandarin.yanille.quest.itwatchtower.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Grew dialogue.
 *
 * # Relations
 * - [Watchtower Quest][content.region.kandarin.yanille.quest.itwatchtower.Watchtower]
 */
@Initializable
class GrewDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        val questStage = getQuestStage(player, Quests.WATCHTOWER)

        if (questStage == 100) {
            sendMessage(player, "The ogre is not interested in you anymore.")
            return true
        }

        if (questStage == 0) {
            sendMessage(player, "The ogre has nothing to say at the moment...")
            return true
        }

        if (getAttribute(player, GameAttributes.WATCHTOWER_GORAD_TOOTH, false)) {
            npc(FaceAnim.OLD_NEUTRAL, "The morsel is back. Does it have our tooth for us?")
            stage = 10
            return true
        }

        if (getAttribute(player, GameAttributes.WATCHTOWER_RELIC_2, false)) {
            when {
                !inInventory(player, Items.CRYSTAL_2380) -> {
                    player("I've lost the crystal you gave me.")
                    stage = 50
                }
                !inInventory(player, Items.RELIC_PART_2_2374) -> {
                    player("I've lost the relic part you gave me.")
                    stage = 40
                }
                else -> {
                    player("Can I do anything else for you?")
                    stage = 30
                }
            }
            return true
        }

        if (questStage > 1) {
            npc(FaceAnim.OLD_NEUTRAL,
                "What do you want, little morsel? You would look good",
                "on my plate!"
            )
        } else {
            sendMessage(player, "The ogre has nothing to say at the moment...")
        }

        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> player("I want to enter the city of ogres.").also { stage++ }
            1 -> npc(FaceAnim.OLD_NEUTRAL, "Hah! I should eat you instead!").also { stage++ }
            2 -> options("Don't eat me, I can help you.", "You will have to kill me first.").also { stage++ }
            3 -> when (buttonId) {
                1 -> {
                    player("Don't eat me, I can help you.")
                    stage = 5
                }
                2 -> {
                    player("You will have to kill me first.")
                    stage = 20
                }
            }
            5 -> npc(FaceAnim.OLD_NEUTRAL, "What can a morsel like you do for me?").also { stage++ }
            6 -> player("I am a mighty adventurer, slayer of monsters and user", "of magic powers.").also { stage++ }
            7 -> npc(FaceAnim.OLD_NEUTRAL, "Well well, perhaps the morsel can help after all...").also { stage++ }
            8 -> npc(FaceAnim.OLD_NEUTRAL, "If you think you're tough", "find Gorad my enemy in the south east settlement", "And knock one of his teeth out!"). also {
                setAttribute(player, GameAttributes.WATCHTOWER_GORAD_TOOTH, true)
                stage++
            }
            9 -> npc(FaceAnim.OLD_LAUGH1, "Heheheheh!").also { stage = END_DIALOGUE }
            10 -> {
                if (!removeItem(player, Items.OGRE_TOOTH_2377)) {
                    player("Err, I don't have it.")
                    stage = 15
                } else {
                    player(FaceAnim.HAPPY, "I have it.")
                    stage++
                }
            }
            11 -> npc(FaceAnim.OLD_NEUTRAL, "It's got it, good good. That'll annoy Gorad lots!", "Heheheheh!").also { stage++ }
            12 -> npc(FaceAnim.OLD_DEFAULT, "Here's a token of my gratitude: some old gem I stole", "from Gorad. And an old part of statue.").also { stage++ }
            13 -> {
                end()
                sendDoubleItemDialogue(player, Items.RELIC_PART_2_2374, Items.CRYSTAL_2380, "You get part of a horrible statue... and one of the crystals!")
                setAttribute(player, GameAttributes.WATCHTOWER_RELIC_2, true)
                addItemOrDrop(player, Items.CRYSTAL_2380)
                addItemOrDrop(player, Items.RELIC_PART_2_2374)
            }
            15 -> npc(FaceAnim.OLD_NEUTRAL, "Morsel, you dare to return without the tooth!", "Either you are a fool, or want to be eaten!").also { stage = END_DIALOGUE }
            20 -> npc(FaceAnim.OLD_NEUTRAL, "That can be arranged - guards!!").also {
                end()
                npc!!.attack(player)
                sendMessage(player, "You are under attack!")
            }
            30 -> npc(FaceAnim.OLD_NEUTRAL, "I have nothing left for you but the cooking pot!").also { stage = END_DIALOGUE }
            40 -> {
                if (inInventory(player, Items.RELIC_PART_2_2374)) {
                    npc(FaceAnim.OLD_NEUTRAL, "You lie to me morsel!")
                    stage = END_DIALOGUE
                } else {
                    npc(FaceAnim.OLD_NEUTRAL, "Stupid morsel, I have another", "Take it and go now before I lose my temper.").also {
                        addItem(player, Items.RELIC_PART_2_2374)
                        stage = END_DIALOGUE
                    }
                }
            }
            50 -> {
                if (inInventory(player, Items.CRYSTAL_2380)) {
                    npc(FaceAnim.OLD_NEUTRAL, "How dare you lie to me Morsel!", "I will finish you now!")
                    stage = END_DIALOGUE
                } else {
                    npc(FaceAnim.OLD_NEUTRAL, "I suppose you want another?", "I suppose just this once I could give you my copy...").also {
                        addItem(player, Items.CRYSTAL_2380)
                        stage = END_DIALOGUE
                    }
                }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.GREW_854)
}
