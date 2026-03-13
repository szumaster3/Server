package content.region.kandarin.yanille.quest.itwatchtower

import content.data.GameAttributes
import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Components
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class Watchtower : Quest(Quests.WATCHTOWER, 131, 130, 4, Vars.VARP_QUEST_WATCHTOWER_PROGRESS_212, 0, 1, 13) {

    private val evidences = intArrayOf(
        Items.FINGERNAILS_2384,
        Items.DAMAGED_DAGGER_2387,
        Items.TATTERED_EYE_PATCH_2388,
        Items.OLD_ROBE_2385,
        Items.UNUSUAL_ARMOUR_2386,
    )

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 12

        if (stage == 0) {
            line(player, "I can start this quest by speaing to the !!Watchtower??", line++)
            line(player, "!!wizard?? on the top floor of Yanille's !!Watchtower??.", line++)
            line++
            line(player, "To complete this quest I need:", line++)
            line(player, "!!Level 14 Magic??", line++, hasLevelStat(player, Skills.MAGIC, 14))
            line(player, "!!Level 40 Mining??", line++, hasLevelStat(player, Skills.MINING, 40))
            line(player, "!!Level 14 Herblore??", line++, hasLevelStat(player, Skills.HERBLORE, 14))
            line(player, "!!Level 15 Thieving??", line++, hasLevelStat(player, Skills.THIEVING, 15))
            line(player, "!!Level 25 Agility??", line++, hasLevelStat(player, Skills.AGILITY, 25))
            line(player, "The north west guard wants a !!sign of friendship??.", line++)
            limitScrolling(player, line, true)
        }

        if (stage >= 1) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I need to collect some evidence by !!searching the bushes??.", line++, allInInventory(player, *evidences) || stage >= 2)

            if (evidences.all { inInventory(player, it) }) {
                line(player, "I have collected all the evidences.", line++, true)
            } else {
                evidences.forEach { itemId ->
                    val item = getItemName(itemId)
                    if (inInventory(player, itemId)) {
                        line(player, "I found some $item as evidence.", line++, true)
                    }
                }
            }
            line(player, "I should take them to the !!Watchtower Wizard??.", line++)
            line++
        }

        if(stage == 10) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given a map by the guard.", line++, true)
            line++
            if(getAttribute(player, "watchtower-cave", false)) {
                line(player, "I found my way into the skavid caves.", line++, true)
                line++
            }
            line(player, "I need to search the skavid caves.", line++)
            line++
        }

        if (stage >= 20) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given a map by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I need to distract the !!enclave guard??.", line++)
            line++
        }

        if (stage == 60) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given !!a map?? by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I used some cave nightshade to distract", line++, true)
            line(player, "the enclave guard.", line++, true)
            line++
            line(player, "I need to defeat the !!ogre shamans??", line++)
            line(player, "and find the other !!crystals??.", line++)
        }

        if(stage == 70) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given !!a map?? by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I used some cave nightshade to distract", line++, true)
            line(player, "the enclave guard.", line++, true)
            line++
            line(player, "I tried to defeat the shamans, but they are", line++)
            line(player, "protected by powerful magics!", line++)
            line(player, "I should speak with the !!Watchtower Wizard?? to see if", line++)
            line(player, "he has any advice for me.", line++)
        }

        if(stage == 75) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given a map by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I used some cave nightshade to distract", line++, true)
            line(player, "the enclave guard.", line++, true)
            line++
            line(player, "I need to make a potion to !!destroy the shamans??.", line++)
            line(player, "I have to gather the ingredients I know about.", line++)
            line++
            val ingredients = listOf(
                Items.POTION_2394 to "base potion",
                Items.CLEAN_GUAM_249 to "guam leaf",
                Items.JANGERBERRIES_247 to "jangerberries",
                Items.GROUND_BAT_BONES_2391 to "ground bat bones"
            )

            ingredients.forEach { (id, name) ->
                if (inInventory(player, id)) {
                    line(player, "I have collected the $name.", line++, true)
                } else {
                    line(player, "I still need to collect the !!$name??", line++)
                }
            }

            if (allInInventory(player, *ingredients.map { it.first }.toIntArray())) {
                line(player, "I have collected all the ingredients.", line++)
                line(player, "I can now give them to the !!Watchtower Wizard?? to make the magic ogre potion.", line++)
            }
        }

        if(stage == 80) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given a map by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I used some cave nightshade to distract", line++, true)
            line(player, "the enclave guard.", line++, true)
            line++
            line(player, "I need to make a potion to destroy the shamans.", line++, true)
            line(player, "I have gathered the ingredients I know about.", line++, true)
            line(player, "I have collected all the ingredients.", line++, true)
            line++
            line(player, "I have made the ogre potion.", line++, true)
            line(player, "I need to gave the potion to the wizard.", line++)
            line++
        }

        if(stage == 85) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given a map by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I used some cave nightshade to distract", line++, true)
            line(player, "the enclave guard.", line++, true)
            line++
            line(player, "I need to make a potion to destroy the shamans.", line++, true)
            line(player, "I have gathered the ingredients I know about.", line++, true)
            line(player, "I have collected all the ingredients.", line++, true)
            line++
            line(player, "I have made the ogre potion.", line++, true)
            line++
            line(player, "I gave the potion to the wizard.", line++, true)
            line(player, "He infused it into a magic ogre potion.", line++, true)
            line(player, "I need to defeat the ogre shamans.", line++)
        }

        if(stage == 90) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given a map by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I used some cave nightshade to distract", line++, true)
            line(player, "the enclave guard.", line++, true)
            line++
            line(player, "I need to make a potion to destroy the shamans.", line++, true)
            line(player, "I have gathered the ingredients I know about.", line++, true)
            line(player, "I have collected all the ingredients.", line++, true)
            line++
            line(player, "I have made the ogre potion.", line++, true)
            line++
            line(player, "I gave the potion to the wizard.", line++, true)
            line(player, "He infused it into a magic ogre potion.", line++, true)
            line(player, "I killed all the ogre shamans.", line++, true)
            line++
            line(player, "I need to return all the crystals to the !!Watchtower Wizard??.", line++)
            line++
        }

        if(stage == 95) {
            line(player, "I accepted the challenge of finding the lost crystals.", line++, true)
            line++
            line(player, "I have spoken to Watchtower Wizard.", line++, true)
            line++
            line(player, "I have collected all the evidences.", line++, true)
            line++
            line(player, "I was given a map by the guard.", line++, true)
            line++
            line(player, "I found my way into the skavid caves.", line++, true)
            line++
            line(player, "I used some cave nightshade to distract", line++, true)
            line(player, "the enclave guard.", line++, true)
            line++
            line(player, "I need to make a potion to destroy the shamans.", line++, true)
            line(player, "I have gathered the ingredients I know about.", line++, true)
            line(player, "I have collected all the ingredients.", line++, true)
            line++
            line(player, "I have made the ogre potion.", line++, true)
            line++
            line(player, "I gave the potion to the wizard.", line++, true)
            line(player, "He infused it into a magic ogre potion.", line++, true)
            line(player, "I killed all the ogre shamans.", line++, true)
            line++
            line(player, "I have returned all the crystals to the Watchtower Wizard.", line++, true)
            line++
            line(player, "I need to use the four power crystals on", line++)
            line(player, "the pillars of the !!shield generator??", line++)
            line(player, "and throw the lever to activate the system.", line++)
        }

        if (stage == 100) {
            line(player, "I helped the wizards in Yanille, and rescued the lost crystals.", line++, true)
            line(player, "I defeated the ogres and their minions.", line++, true)
            line(player, "I read the scroll and gained a new teleport spell.", line++, getAttribute(player, GameAttributes.WATCHTOWER_TELEPORT, false))
            line++
            line(player, "I can return to the !!Watchtower?? at any time. My task here is done.", line++)
            line++
            line(player, "<col=FF0000>QUEST COMPLETE!</col>", line, false)
            limitScrolling(player, line, false)
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var ln = 10
        sendItemOnInterface(player, Components.QUEST_COMPLETE_SCROLL_277, 5, Items.OGRE_RELIC_2372, 1)
        drawReward(player, "4 Quest Points", ln++)
        drawReward(player, "Watchtower Teleport spell", ln++)
        drawReward(player, "15,250 Magic XP", ln++)
        drawReward(player, "5000 coins", ln)
        rewardXP(player, Skills.MAGIC, 15250.0)
        addItemOrDrop(player, Items.COINS_995, 5000)
        addItem(player, Items.SPELL_SCROLL_2396, 1)
        removeAttributes(player, GameAttributes.WATCHTOWER_ROCK_CAKE)
    }

    override fun newInstance(`object`: Any?): Quest = this
}