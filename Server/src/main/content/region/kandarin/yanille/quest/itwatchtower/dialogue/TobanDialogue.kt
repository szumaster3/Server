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
 * Represents the Toban dialogue.
 *
 * # Relations
 * - [Watchtower Quest][content.region.kandarin.yanille.quest.itwatchtower.Watchtower]
 */
@Initializable
class TobanDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        val questStage = getQuestStage(player, Quests.WATCHTOWER)

        if (questStage == 100) {
            sendMessage(player, "The ogre is not interested in you anymore.")
            return true
        }

        if (questStage == 0) {
            sendMessage(player, "He's busy, try him later.")
            return true
        }

        if (getAttribute(player, GameAttributes.WATCHTOWER_RELIC_3, false)) {
            player("I can't find the relic part you gave me.")
            stage = 14
            return true
        }

        if (getAttribute(player, GameAttributes.WATCHTOWER_DRAGON_BONES, false)) {
            npc(FaceAnim.OLD_NEUTRAL, "Hahaha! Small t'ing returns. Did you bring the dragon", "bone?")
            stage = 10
            return true
        }

        if (questStage > 1) {
            npc(FaceAnim.OLD_NEUTRAL, "What do you want, small t'ing?")
        }

        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("I seek entrance to the city of ogres.", "Die, creature!").also { stage++ }
            1 -> when (buttonId) {
                1 -> player("I seek entrance to the city of ogres.").also { stage += 2 }
                2 -> player("Die, creature!").also { stage++ }
            }
            2 -> npcl(FaceAnim.OLD_NEUTRAL, "Hahaha! It t'inks it's a match for Toban, does it?").also {
                end()
                npc!!.attack(player)
                sendMessage(player, "You are under attack!")
            }
            3 -> npc(FaceAnim.OLD_NEUTRAL, "Hahaha! You'll never get in there.").also { stage++ }
            4 -> player("I'll find a way, trust me.").also { stage++ }
            5 -> npc(FaceAnim.OLD_NEUTRAL, "Bold words for a t'ing so small.").also { stage++ }
            6 -> options("I could do something for you...", "Die, creature!").also { stage++ }
            7 -> when (buttonId) {
                1 -> player("I could do something for you...").also { stage++ }
                2 -> player("Die, creature!").also { stage = 2 }
            }
            8 -> npc(FaceAnim.OLD_NEUTRAL, "Hahaha! This creature t'inks it can help me! I would eat", "you now, but for your punt size.").also { stage++ }
            9 -> npc(FaceAnim.OLD_NEUTRAL, "Prove to me your might: bring me the bones of an", "adult dragon to chew on, and I'll eat them, not you.").also {
                setAttribute(player, GameAttributes.WATCHTOWER_DRAGON_BONES, true)
                stage = END_DIALOGUE
            }
            10 -> {
                if (!inInventory(player, Items.DRAGON_BONES_536)) {
                    player("I have nothing for you.")
                    stage = 13
                    return true
                }

                if (freeSlots(player) == 0) {
                    npcl(FaceAnim.OLD_NEUTRAL, "Hahaha! Small t'ing has done it. Toban is glad, but you can't hold his reward; you'd best put somet'ing down.")
                    stage = END_DIALOGUE
                    return true
                }
                player(FaceAnim.FRIENDLY,"When I say I will get something, I get it!")
                stage = 11
            }

            11 -> {
                npcl(FaceAnim.OLD_NEUTRAL, "Hahaha! Small t'ing has done it. Toban is glad - take this...")
                stage = 12
            }

            12 -> {
                if (removeItem(player, Items.DRAGON_BONES_536)) {
                    addItem(player, Items.RELIC_PART_3_2375)
                    sendItemDialogue(player, Items.RELIC_PART_3_2375, "The ogre gives you part of a statue.")
                    setAttribute(player, GameAttributes.WATCHTOWER_RELIC_3, true)
                }
                stage = END_DIALOGUE
            }

            13 -> npc(FaceAnim.OLD_NEUTRAL, "Then you shall get nothing from me!").also { stage = END_DIALOGUE }
            14 -> {
                if (!inInventory(player, Items.RELIC_PART_3_2375)) {
                    npc(FaceAnim.OLD_NEUTRAL, "Small thing, how could you be so careless?")
                    stage = 16
                } else {
                    npc(FaceAnim.OLD_NEUTRAL, "Small thing, you lie to me!")
                    stage++
                }
            }

            15 -> npc(FaceAnim.OLD_NEUTRAL, "I always says that small things are big trouble...").also { stage = END_DIALOGUE }
            16 -> npc(FaceAnim.OLD_NEUTRAL, "Here, take this one.").also { stage++ }
            17 -> {
                end()
                sendItemDialogue(player, Items.RELIC_PART_3_2375, "The ogre gives you part of a statue.")
                addItem(player, Items.RELIC_PART_3_2375)
            }

        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = TobanDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.TOBAN_855)
}
