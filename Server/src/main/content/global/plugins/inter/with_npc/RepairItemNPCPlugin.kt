package content.global.plugins.inter.with_npc

import content.data.items.RepairItem
import content.global.plugins.item.equipment.BarrowsEquipment
import content.region.misthalin.lumbridge.dialogue.BobDialogue
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.ClassScanner
import shared.consts.NPCs

private val REPAIRABLE_ITEM_IDS = (RepairItem.repairableItemIds + BarrowsEquipment.getAllRepairableBarrowsIds()).toIntArray()
private val NPC_IDS = intArrayOf(NPCs.BOB_519, NPCs.SQUIRE_3797, NPCs.TINDEL_MARCHANT_1799)

class RepairItemNPCPlugin : InteractionListener {

    override fun defineListeners() {
        ClassScanner.definePlugin(BobDialogue())

        onUseWith(IntType.NPC, REPAIRABLE_ITEM_IDS, *NPC_IDS) { player, used, with ->
            handleRepair(player, used.asItem(), with.asNpc())
            return@onUseWith true
        }
    }

    private fun handleRepair(player: Player, usedItem: Item, npc: NPC) {
        val repairItem = RepairItem.forId(usedItem.id)

        if (repairItem == null && !isBarrowsItem(usedItem.id)) {
            player.dialogueInterpreter.open(NPCs.BOB_519, npc, true, true, null)
        } else {
            player.dialogueInterpreter.open(NPCs.BOB_519, npc, true, false, usedItem.id, usedItem)
        }
    }

    private fun isBarrowsItem(itemId: Int): Boolean = BarrowsEquipment.isBarrowsItem(itemId)
}