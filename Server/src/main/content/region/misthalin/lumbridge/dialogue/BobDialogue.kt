package content.region.misthalin.lumbridge.dialogue

import content.data.items.RepairItem
import content.global.plugins.item.equipment.BarrowsEquipment
import core.api.openNpcShop
import core.api.sendNPCDialogue
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.Diary
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.item.Item
import core.game.world.GameWorld
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents the Bob dialogue.
 */
class BobDialogue(player: Player? = null) : Dialogue(player) {
    /**
     * Represents the item id being repaired.
     */
    private var itemId = 0

    /**
     * Represents the item being repaired.
     */
    private lateinit var item: Item

    /**
     * Represents the item repairing.
     */
    private var repairItem: RepairItem? = null

    /**
     * The achievement diary.
     */
    private val level = 1

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.FURIOUS, "Get yer own!").also { stage = END_DIALOGUE }
            1 -> npc(FaceAnim.HAPPY, "Yes! I buy and sell axes! Take your pick or axe!").also { stage++ }
            2 -> end().also { openNpcShop(player, npc.id) }
            3 -> sendNPCDialogue(player, npc.id, "Of course I'll repair it, though the materials may cost you. Just hand me the item and I'll have a look.", FaceAnim.FRIENDLY).also { stage = END_DIALOGUE }
            4 -> when {
                Diary.canClaimLevelRewards(player, DiaryType.LUMBRIDGE, level) -> player("I've done all the medium tasks in my Lumbridge", "Achievement Diary.").also { stage = 13 }
                Diary.canReplaceReward(player, DiaryType.LUMBRIDGE, level) -> player("I've seemed to have lost my explorer's ring...").also { stage = 17 }
                else -> showTopics(
                    Topic("What is the Achievement Diary?",5),
                    Topic("What are the rewards?",9),
                    Topic("How do I claim the rewards?",11),
                    Topic("See you later.", END_DIALOGUE)
                )
            }
            6 ->  npcl(FaceAnim.FRIENDLY, "Ah, well it's a diary that helps you keep track of particular achievements you've made in the world of " + GameWorld.settings!!.name + ". In Lumbridge and Draynor i can help you discover some very useful things indeed.").also { stage++ }
            7 ->  npc(FaceAnim.FRIENDLY, "Eventually with enough exploration you will be", "rewarded for your explorative efforts.").also { stage++ }
            8 ->  npc(FaceAnim.FRIENDLY, "You can access your Achievement Diary by going to", "the Quest Journal. When you've opened the Quest", "Journal click on the green star icon on the top right", "hand corner. This will open the diary.").also { stage = 4 }
            9 ->  npc(FaceAnim.FRIENDLY, "Ah, well there are different rewards for each", "Achievement Diary. For completing the Lumbridge and", "Draynor diary you are presented with an explorer's", "ring.").also { stage++ }
            10 -> npc(FaceAnim.FRIENDLY, "This ring will become increasingly useful with each", "section of the diary that you complete.").also { stage = 4 }
            11 -> npc(FaceAnim.FRIENDLY, "You need to complete the tasks so that they're all ticked", "off, then you can claim your reward. Most of them are", "straightforward although you might find some required", "quests to be started, if not finished.").also { stage++ }
            12 -> npc(FaceAnim.FRIENDLY, "To claim the explorer's ring speak to Explorer Jack", " in Lumbridge, Ned in Draynor Village or myself.").also { stage = 4 }
            13 -> npc(FaceAnim.FRIENDLY, "Yes I see that, you'll be wanting your", "reward then I assume?").also { stage++ }
            14 -> player(FaceAnim.HALF_GUILTY, "Yes please.").also { stage++ }
            15 -> {
                Diary.flagRewarded(player, DiaryType.LUMBRIDGE, level)
                npc(FaceAnim.FRIENDLY, "This ring is a representation of the adventures you", "went on to complete your tasks.").also { stage++ }
            }
            16 -> player(FaceAnim.HAPPY, "Wow, thanks!").also { stage = END_DIALOGUE }
            17 -> {
                Diary.grantReplacement(player, DiaryType.LUMBRIDGE, level)
                npc(FaceAnim.FRIENDLY, "You better be more careful this time.")
                stage = END_DIALOGUE
            }
            18 -> showTopics(
                Topic("Yes, please.",20),
                Topic("No, thank you.",19),
            )
            19 -> player(FaceAnim.NEUTRAL, "On second thoughts, no thanks.").also { stage = END_DIALOGUE }
            20 -> {
                end()
                val repairItems = RepairItem.forId(itemId)

                if (repairItems != null) {
                    if (!player.inventory.contains(Items.COINS_995, repairItems.cost)) {
                        player.packetDispatch.sendMessage("You don't have enough to pay him.")
                        return true
                    }
                    if (player.inventory.remove(Item(itemId, 1))) {
                        player.inventory.remove(Item(Items.COINS_995, repairItems.cost))
                        player.inventory.add(repairItems.product)
                        val costText = if (repairItems.cost > 0) "${repairItems.cost}gp" else "free"
                        player.packetDispatch.sendMessage(
                            "Bob fixes your ${
                                item.name.lowercase().replace("broken", "").trim()
                            } for $costText."
                        )
                    }
                } else {
                    val barrowsDef = BarrowsEquipment.getDefinition(itemId) ?: return true
                    val repairCost = BarrowsEquipment.getRepairCost(item)

                    if (!player.inventory.contains(Items.COINS_995, repairCost)) {
                        player.packetDispatch.sendMessage("You don't have enough to pay him.")
                        return true
                    }
                    if (player.inventory.remove(Item(itemId, 1))) {
                        player.inventory.remove(Item(Items.COINS_995, repairCost))
                        player.inventory.add(Item(barrowsDef.repairedId))
                        val costText = if (repairCost > 0) "${repairCost}gp" else "free"
                        player.packetDispatch.sendMessage("Bob fixes your ${barrowsDef.itemName.lowercase()} for $costText.")
                    }
                }
            }
        }
        return true
    }

    override fun newInstance(player: Player): Dialogue {
        return BobDialogue(player)
    }

    override fun open(vararg args: Any): Boolean {
        npc = args[0] as NPC
        var repair = false
        var wrong = false
        if (npc.id == NPCs.SQUIRE_3797 && args.size == 1) {
            player("Can you repair my items for me?")
            stage = 3
            return true
        }
        if (args.size == 1) {
            showTopics(
                Topic("Give me a quest!", 0),
                Topic("Have you anything to sell?", 1),
                Topic("Can you repair my items for me?", 3),
                Topic("Talk about Achievement Diaries", 4),
            )
            stage = 0
            return true
        }

        if (args.size > 1) repair = args[1] as Boolean
        if (args.size > 2) wrong = args[2] as Boolean
        if (args.size > 3) {
            repairItem = RepairItem.forId(args[3] as Int)
            itemId = args[3] as Int
        }
        if (args.size > 4) item = args[4] as Item

        if (repair && !wrong) {
            val cost = RepairItem.forId(itemId)?.cost ?: BarrowsEquipment.getRepairCost(item)
            val costText = if (cost > 0) "${cost}gp" else "free"
            npc("Quite badly damaged, but easy to repair. Would you", "like me to repair it for $costText?")
            stage = 18
            return true
        }

        if (repair && wrong) {
            npc("Sorry friend, but I can't do anything with that.")
            stage = END_DIALOGUE
            return true
        }

        return true
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.BOB_519, NPCs.SQUIRE_3797, NPCs.TINDEL_MARCHANT_1799)
    }
}
