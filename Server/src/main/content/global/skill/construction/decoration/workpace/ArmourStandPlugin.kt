package content.global.skill.construction.decoration.workpace

import content.data.items.RepairItem
import content.data.items.RustyItem
import content.global.plugins.item.equipment.BarrowsEquipment
import core.api.*
import core.game.dialogue.Dialogue
import core.game.interaction.NodeUsageEvent
import core.game.interaction.UseWithHandler
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery
import kotlin.math.ceil

/**
 * Represents the broken items that can be exchanged.
 */
private val BROKEN_ITEM_IDS = listOf(
    Items.RUSTY_SWORD_686,
    Items.BROKEN_ARROW_687,
    Items.DAMAGED_ARMOUR_697,
    Items.BROKEN_ARMOUR_698,
    Items.BROKEN_STAFF_689
)

/**
 * Represents the items that can be repaired on the stand.
 */
private val REPAIRABLE_ITEM_IDS = (
        RepairItem.repairableItemIds +
                BROKEN_ITEM_IDS +
                BarrowsEquipment.getAllRepairableBarrowsIds()
        ).toIntArray()

/**
 * Handles interactions with armor repair stands.
 * Supports repairing broken items, rusty equipment, and Barrows items.
 */
@Initializable
class ArmourStandPlugin : UseWithHandler(*REPAIRABLE_ITEM_IDS)
{
    private val rustyMap = mapOf(
        Items.BROKEN_ARROW_687   to RustyItem.EquipmentType.ARROWS,
        Items.BROKEN_STAFF_689   to RustyItem.EquipmentType.STAVES,
        Items.RUSTY_SWORD_686    to RustyItem.EquipmentType.SWORDS,
        Items.DAMAGED_ARMOUR_697 to RustyItem.EquipmentType.ARMOUR,
        Items.BROKEN_ARMOUR_698  to RustyItem.EquipmentType.LEGS
    )

    private val objectMap = mapOf(
        Scenery.REPAIR_BENCH_13713  to listOf(Items.BROKEN_ARROW_687, Items.BROKEN_STAFF_689),
        Scenery.WHETSTONE_13714     to listOf(Items.BROKEN_ARROW_687, Items.BROKEN_STAFF_689, Items.RUSTY_SWORD_686),
        Scenery.ARMOUR_REPAIR_STAND_13715 to BROKEN_ITEM_IDS
    )

    override fun newInstance(arg: Any?): Plugin<Any>
    {
        objectMap.keys.forEach { addHandler(it, OBJECT_TYPE, this) }
        return this
    }

    override fun handle(event: NodeUsageEvent?): Boolean {
        event ?: return false
        val player = event.player
        val item = event.used.asItem()

        if (item.id !in REPAIRABLE_ITEM_IDS) return false

        lock(player, 3)
        val allowedItems = objectMap[event.usedWith.id] ?: emptyList()
        if (item.id in rustyMap.keys && item.id in allowedItems) {
            exchangeRustyItem(player, item)
        } else if (event.usedWith.id == Scenery.ARMOUR_REPAIR_STAND_13715) {
            handleArmourStandRepair(player, item)
        } else {
            player.sendMessage("You can't do that.")
        }

        return true
    }

    private fun exchangeRustyItem(player: Player, item: Item)
    {
        val repairType = rustyMap[item.id] ?: return
        RustyItem.getRepair(repairType)?.let { repairedItem ->
            player.animate(Animation(Animations.CHURN_PLUMING_STAND_3654))
            player.inventory.remove(item)
            player.inventory.add(repairedItem)
            sendItemDialogue(player, repairedItem.id,
                "You repair the ${getItemName(item.id)} and find it is a ${repairedItem.name.lowercase()}.")
        }
    }

    private fun handleArmourStandRepair(player: Player, item: Item)
    {
        val repairTool = RepairItem.forId(item.id)
        val barrowsItem = BarrowsEquipment.getDefinition(item.id)

        val (product, baseCost) = when
        {
            repairTool != null -> repairTool.product to repairTool.cost
            barrowsItem != null -> {
                if (BarrowsEquipment.isFullyRepaired(item.id))
                {
                    player.sendMessage("That item can't be repaired.")
                    return
                }
                Item(barrowsItem.repairedId) to BarrowsEquipment.getRepairCost(item)
            }
            else -> {
                player.sendMessage("That item can't be repaired.")
                return
            }
        }

        val discount = (100.0 - player.skills.getLevel(Skills.SMITHING) / 2.0) / 100.0
        val cost = ceil(discount * baseCost).toInt()
        player.dialogueInterpreter.open(58824213, item, cost, product)
    }

    @Initializable
    class RepairDialogue(player: Player? = null) : Dialogue(player)
    {
        private var item: Item? = null
        private var cost: Int = 0
        private var product: Item? = null

        override fun newInstance(player: Player?): Dialogue = RepairDialogue(player)

        override fun open(vararg args: Any?): Boolean
        {
            item = args[0] as Item
            cost = args[1] as Int
            product = args[2] as Item

            val itemName = getItemName(item!!.id)
            sendDialogue(player!!, "Would you like to repair your $itemName for $cost gp?")
            stage = 0
            return true
        }

        override fun handle(interfaceId: Int, buttonId: Int): Boolean
        {
            val currentItem = item ?: return false
            val currentProduct = product ?: return false

            when (stage)
            {
                0 -> options("Yes, please", "No, thanks.").also { stage++ }
                1 -> when (buttonId) {
                    1 -> {
                        exchangeItems(currentItem, cost, currentProduct)
                        end()
                    }

                    2 -> end()
                }
            }
            return true
        }

        private fun exchangeItems(item: Item, cost: Int, product: Item)
        {
            val coins = Item(Items.COINS_995, cost)
            if (player.inventory.containsItem(coins) && player.inventory.containsItem(item))
            {
                if (player.inventory.remove(item, coins))
                {
                    if (player.inventory.add(product))
                    {
                        player.animate(Animation(Animations.CHURN_PLUMING_STAND_3654))
                        val costText = if (cost > 0) "${cost}gp" else "free"
                        player.sendMessage("You repair your ${product.name.lowercase()} for $costText.")
                        return
                    }
                }
            }
            else
            {
                player.sendMessage("You can't afford that.")
            }
        }

        override fun getIds(): IntArray
        {
            return intArrayOf(58824213)
        }
    }
}
