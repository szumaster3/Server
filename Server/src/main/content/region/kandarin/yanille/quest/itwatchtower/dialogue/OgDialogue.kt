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
 * Represents the Og dialogue.
 *
 * # Relations
 * - [Watchtower Quest][content.region.kandarin.yanille.quest.itwatchtower.Watchtower]
 */
@Initializable
class OgDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        val stage = getQuestStage(player, Quests.WATCHTOWER)

        when {
            stage == 100 -> {
                sendMessage(player, "The ogre is not interested in you anymore.")
                return true
            }

            stage == 0 -> {
                sendMessage(player, "He's busy, try him later.")
                return true
            }

            getAttribute(player, GameAttributes.WATCHTOWER_TOBAN_GOLD, false) -> {
                npc(FaceAnim.OLD_DEFAULT, "Where my gold from dat dirty Toban?")
                this.stage = 9
                return true
            }

            getAttribute(player, GameAttributes.WATCHTOWER_RELIC_1, false) -> {
                npc(FaceAnim.OLD_DEFAULT, "It's the little rat again.")
                this.stage = 16
                return true
            }

            stage == 2 -> {
                npc(FaceAnim.OLD_DEFAULT, "Why you here, little t'ing?")
                return true
            }

            else -> {
                sendMessage(player, "The ogre has nothing to say at the moment.")
                return true
            }
        }
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("I seek entrance to the city of ogres.", "I have come to kill you.").also { stage++ }
            1 -> when (buttonId) {
                1 -> player("I seek entrance to the city of ogres.").also { stage++ }
                2 -> player("I have come to kill you!").also { stage = 7 }
            }
            2 -> npc(FaceAnim.OLD_DEFAULT, "You got no business there!").also { stage++ }
            3 -> npc(FaceAnim.OLD_DEFAULT, "Just a minute... Maybe if you did somet'ing for me, I", "might help you get in.").also { stage++ }
            4 -> player("What can I do to help an ogre?").also { stage++ }
            5 -> npc(FaceAnim.OLD_DEFAULT, "South-east of here der is more ogres - the name of the", "chieftain is Toban. He stole gold from me and I want it", "back!").also { stage++ }
            6 -> npc(FaceAnim.OLD_DEFAULT, "Here is a key to the chest it's in. If you bring it here,", "I may reward you...").also {
                end()
                addItemOrDrop(player, Items.TOBANS_KEY_2378, 1)
                setAttribute(player, GameAttributes.WATCHTOWER_TOBAN_GOLD, true)
            }
            7 -> npc(FaceAnim.OLD_DEFAULT, "Kill me, eh? You shall be crushed. Guards!").also { stage++ }
            8 -> {
                end()
                npc!!.attack(player)
                sendMessage(player, "You are under attack!")
            }
            9 -> options("I have your gold.", "I haven't got it yet.", "I have lost the key!").also { stage++ }
            10 -> when(buttonId) {
                1 -> player("I have your gold.").also { stage++ }
                2 -> player("I haven't got it yet.").also { stage = 13 }
                3 -> player("I have lost the key!").also { stage = 14 }
            }
            11 -> {
                if (!inInventory(player, Items.TOBANS_GOLD_2393)) {
                    npc(FaceAnim.OLD_DEFAULT, "Dat is not what I want! If you want to impress me,", "get da gold I asked for!")
                    stage = END_DIALOGUE
                } else if (freeSlots(player) == 0) {
                    npc(FaceAnim.OLD_NEUTRAL, "Hah! This little t'ing is pretty smart for such a little t'ing!", "Pity you don't have any space for your reward.")
                    stage = END_DIALOGUE
                } else {
                    npc(FaceAnim.OLD_DEFAULT, "Well, well, the little t'ing has got it!", "Take dis to show", "little t'ing is a friend to da ogres. Hahahahaha!")
                    stage = 12
                }
            }
            12 -> {
                end()
                removeItem(player, Items.TOBANS_GOLD_2393)
                sendItemDialogue(player, Items.RELIC_PART_1_2373, "The ogre gives you part of a horrible statue.")
                setAttribute(player, GameAttributes.WATCHTOWER_RELIC_1, true)
                addItem(player, Items.RELIC_PART_1_2373)
            }
            13 -> npcl(FaceAnim.OLD_DEFAULT, "Don't come back until you have it, unless you wanna be on tonight's menu!").also { stage = END_DIALOGUE }
            14 -> {
                if (inInventory(player, Items.TOBANS_KEY_2378)) {
                    npc(FaceAnim.OLD_DEFAULT, "Oh yeah? What's dat den?")
                    stage = 15
                    return true
                } else {
                    end()
                    npc(FaceAnim.OLD_DEFAULT, "Stoopid! Take another and don't lose it!")
                    addItem(player, Items.TOBANS_KEY_2378, 1)
                    stage = END_DIALOGUE
                }
            }
            15 -> {
                end()
                sendMessage(player, "It seems you still have the key...")
            }
            16 -> options("Do you have any other tasks for me?", "I have lost the relic part you gave me.").also { stage++ }
            17 -> when(buttonId) {
                1 -> player("Do you have any other tasks for me?")
                2 -> player(FaceAnim.HALF_WORRIED, "I have lost the relic part you gave me.").also { stage++ }
            }
            18 -> {
                if (inInventory(player, Items.RELIC_PART_1_2373)) {
                    npcl(FaceAnim.OLD_DEFAULT, "Are you blind! I can see you have it even from here!")
                    stage = END_DIALOGUE
                } else {
                    npc(FaceAnim.OLD_DEFAULT, "Grrr, why do I bother?")
                    stage++
                }
            }
            19 -> {
                end()
                npc(FaceAnim.OLD_DEFAULT,"It's a good job I have another part!")
                addItem(player, Items.RELIC_PART_1_2373)
            }

        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.OG_853)
}
