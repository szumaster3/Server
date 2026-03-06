package content.region.misthalin.draynor.quest.vampire

import core.api.rewardXP
import core.api.sendItemZoomOnInterface
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Components
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class VampireSlayer : Quest(Quests.VAMPIRE_SLAYER, 30, 29, 3, Vars.VARP_QUEST_VAMPIRE_SLAYER_PROGRESS_178, 0, 1, 3) {

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 11

        if (getStage(player) == 0) {
            line(player, "I can start this quest by speaking to !!Morgan who is in??", line++)
            line(player, "!!Draynor Village??.", line++)
            line(player, "Requirements:", line++)
            line(player, "Must be able to kill a level 34 !!Vampire??.", line++)
        }
        if (getStage(player) == 10) {
            line(player, "I spoke to Morgan in Draynor Village. He told me that the", line++, true)
            line(player, "locals are being attacked by a terrifying Vampire!", line++, true)
            line++
            line(player, "I need to speak to !!Dr Harlow?? who can normally be found in", line++,)
            line(player, "the !!Blue Moon Inn?? in !!Varrock??.", line++, true)
        }
        if (getStage(player) == 20) {
            line(player, "I spoke to Morgan in Draynor Village. He told me that the", line++, true)
            line(player, "locals are being attacked by a terrifying Vampire!", line++, true)
            line++
            line(player, "I have spoken to Dr Harlow. He seemed terribly drunk, and", line++, true)
            line(player, "he kept asking me to buy him drinks.", line++, true)
            line++
            line(player, "I should see what advice !!Dr Harlow?? can give me about killing", line++, true)
            line(player, "!!Vampires??.", line++, true)
            line(player, "When I'm ready, I should go to !!Draynor Manor??, north of", line++)
            line(player, "Draynor to kill the !!Vampire?? that's living in the basement.", line++)
        }
        if (getStage(player) == 30) {
            line(player, "I spoke to Morgan in Draynor Village. He told me that the", line++, true)
            line(player, "locals are being attacked by a terrifying Vampire!", line++, true)
            line++
            line(player, "I have spoken to Dr Harlow. He seemed terribly drunk, and", line++, true)
            line(player, "he kept asking me to buy him drinks.", line++, true)
            line++
            line(player, "Dr Harlow gave me a stake to finish off the Vampire then", line++, true)
            line(player, "I'm fighting it. I've got a hammer to drive the stake deep", line++, true)
            line(player, "into the Vampire's chest, and I have some garlic which", line++, true)
            line(player, "should weaken the Vampire.", line++, true)
            line(player, "When i'm ready, I should go to !!Draynor Manor??, north of", line++, true)
            line(player, "Draynor to kill the !!Vampire?? that's living in the basement.", line++, true)
        }
        if (getStage(player) == 100) {
            line(player, "I spoke to Morgan in Draynor Village. He told me that the", line++, true)
            line(player, "locals are being attacked by a terrifying Vampire!", line++, true)
            line++
            line(player, "I have spoken to Dr Harlow. He seemed terribly drunk, and", line++, true)
            line(player, "he kept asking me to buy him drinks.", line++, true)
            line++
            line(player, "I have killed the Vampire, Count Draynor. Draynor Village is", line++, true)
            line(player, "now safe!", line++, true)
            line++
            line(player, "<col=FF0000>QUEST COMPLETE!</col>", line, false)
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var line = 10
        sendItemZoomOnInterface(player, Components.QUEST_COMPLETE_SCROLL_277, 5, Items.STAKE_1549, 260)
        drawReward(player, "3 Quest Point", line++)
        drawReward(player, "4825 Attack XP", line)
        rewardXP(player, Skills.ATTACK, 4825.0)
    }

    override fun newInstance(`object`: Any?): Quest = this
}
