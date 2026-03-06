package core.api

import com.moandjiezana.toml.Toml
import content.data.God
import content.data.consumables.Consumables
import content.data.skill.SkillingTool
import content.global.plugins.iface.ge.StockMarket
import content.global.skill.slayer.SlayerManager
import content.global.skill.slayer.Tasks
import content.global.skill.summoning.familiar.BurdenBeast
import core.ServerConstants
import core.api.utils.PlayerCamera
import core.api.utils.PlayerStatsCounter
import core.api.utils.Vector
import core.cache.def.impl.*
import core.game.activity.Cutscene
import core.game.component.Component
import core.game.consumable.Consumable
import core.game.consumable.Potion
import core.game.container.impl.EquipmentContainer
import core.game.dialogue.*
import core.game.dialogue.DialogueInterpreter.getDialogueKey
import core.game.ge.ExchangeHistory
import core.game.global.action.DoorActionHandler
import core.game.interaction.Clocks
import core.game.interaction.InteractionListeners
import core.game.interaction.QueueStrength
import core.game.interaction.QueuedScript
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.combat.CombatSwingHandler
import core.game.node.entity.combat.ImpactHandler
import core.game.node.entity.impl.Animator
import core.game.node.entity.impl.ForceMovement
import core.game.node.entity.impl.Projectile
import core.game.node.entity.impl.PulseType
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.LogType
import core.game.node.entity.player.info.PlayerMonitor
import core.game.node.entity.player.link.HintIconManager
import core.game.node.entity.player.link.TeleportManager
import core.game.node.entity.player.link.appearance.BodyPart
import core.game.node.entity.player.link.audio.Audio
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.player.link.emote.Emotes
import core.game.node.entity.player.link.prayer.PrayerType
import core.game.node.entity.player.link.quest.*
import core.game.node.entity.skill.Skills
import core.game.node.item.GroundItem
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.node.scenery.SceneryBuilder
import core.game.shops.Shops
import core.game.system.config.ItemConfigParser
import core.game.system.config.ServerConfigParser
import core.game.system.task.Pulse
import core.game.system.timer.RSTimer
import core.game.system.timer.TimerRegistry
import core.game.system.timer.impl.Disease
import core.game.system.timer.impl.DragonFireImmunity
import core.game.system.timer.impl.Poison
import core.game.system.timer.impl.PoisonImmunity
import core.game.world.GameWorld
import core.game.world.GameWorld.Pulser
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.map.RegionManager.getRegionChunk
import core.game.world.map.path.Pathfinder
import core.game.world.map.zone.MapZone
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneBuilder
import core.game.world.repository.Repository
import core.game.world.update.flag.EntityFlag
import core.game.world.update.flag.chunk.AnimateObjectUpdateFlag
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.ForceMoveCtx
import core.game.world.update.flag.context.Graphics
import core.net.packet.PacketRepository
import core.net.packet.context.*
import core.net.packet.out.*
import core.tools.Log
import core.tools.SystemLogger
import core.tools.colorize
import core.tools.cyclesToTicks
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Sounds
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Checks if the player has a level in the specified skill greater than or equal to the given level.
 *
 * @param player The player to check.
 * @param skill The skill to check.
 * @param level The required level to check against.
 * @return True if the player has the level or greater in the specified skill, false otherwise.
 */
fun hasLevelDyn(
    player: Player,
    skill: Int,
    level: Int,
): Boolean = player.skills.getLevel(skill) >= level

/**
 * Checks if the player has a static level in the specified skill greater than or equal to the given level.
 *
 * @param player The player to check.
 * @param skill The skill to check.
 * @param level The required level to check against.
 * @return True if the player has the static level or greater in the specified skill, false otherwise.
 */
fun hasLevelStat(
    player: Player,
    skill: Int,
    level: Int,
): Boolean = player.skills.getStaticLevel(skill) >= level

/**
 * Returns the amount of a specific item in the player's inventory.
 *
 * @param player The player whose inventory is being checked.
 * @param id The ID of the item to check.
 * @return The amount of the specified item in the player's inventory.
 */
fun amountInInventory(
    player: Player,
    id: Int,
): Int = player.inventory.getAmount(id)

/**
 * Returns the total amount of a specific item in the player's bank.
 *
 * @param player The player whose bank is being checked.
 * @param id The ID of the item to check.
 * @return The total amount of the specified item in the player's bank.
 */
fun amountInBank(
    player: Player,
    id: Int,
): Int = player.bank.getAmount(id)

/**
 * Returns the total amount of the item in the player bank.
 *
 * @param player the player whose bank to check.
 * @param id the item id to look for.
 * @return the amount of the item in the bank, or 0 if not found.
 */
private fun getAmountInBank(player: Player, id: Int): Int =
    player.bank.getAmount(Item(id))

/**
 * Returns the amount of a specific item in the player's equipment.
 *
 * @param player The player whose equipment is being checked.
 * @param id The ID of the item to check.
 * @return The amount of the specified item in the player's equipment.
 */
fun amountInEquipment(
    player: Player,
    id: Int,
): Int {
    val slot = itemDefinition(id).getConfiguration(ItemConfigParser.EQUIP_SLOT, -1)
    val equipped = player.equipment[slot] ?: return 0
    return if (equipped.id == id) equipped.amount else 0
}

/**
 * Checks if the player has a specific amount of an item in their inventory.
 *
 * @param player The player whose inventory is being checked.
 * @param id The ID of the item to check.
 * @param amount The required amount of the item.
 * @return True if the player has the specified amount or more of the item in their inventory, false otherwise.
 */
fun inInventory(
    player: Player,
    id: Int,
    amount: Int = 1,
): Boolean = player.inventory.contains(id, amount)

/**
 * Checks if the player has a specific amount of an item in their bank.
 *
 * @param player The player whose bank is being checked.
 * @param id The ID of the item to check.
 * @param amount The required amount of the item.
 * @return True if the player has the specified amount or more of the item in their bank, false otherwise.
 */
fun inBank(
    player: Player,
    id: Int,
    amount: Int = 1,
): Boolean = amountInBank(player, id) >= amount

/**
 * Checks if the player has a specific amount of an item in their equipment.
 *
 * @param player The player whose equipment is being checked.
 * @param id The ID of the item to check.
 * @param amount The required amount of the item.
 * @return True if the player has the specified amount or more of the item in their equipment, false otherwise.
 */
fun inEquipment(
    player: Player,
    id: Int,
    amount: Int = 1,
): Boolean = amountInEquipment(player, id) >= amount

/**
 * Checks if the player has a specific amount of an item in their equipment or inventory.
 *
 * @param player The player whose equipment and inventory are being checked.
 * @param id The ID of the item to check.
 * @param amount The required amount of the item.
 * @return True if the player has the specified amount or more of the item in either their equipment or inventory.
 */
fun inEquipmentOrInventory(
    player: Player,
    id: Int,
    amount: Int = 1,
): Boolean = inEquipment(player, id, amount) || inInventory(player, id, amount)

/**
 * Checks if the player has all specified items in their equipment.
 *
 * @param player The player whose equipment is being checked.
 * @param ids The IDs of the items to check.
 * @return True if the player has all the specified items in their equipment, false otherwise.
 */
fun allInEquipment(
    player: Player,
    vararg ids: Int,
): Boolean = ids.all { id -> inEquipment(player, id) }

/**
 * Checks if the player has any of the specified items in their equipment.
 *
 * @param player The player whose equipment is being checked.
 * @param ids The IDs of the items to check.
 * @return True if the player has at least one of the specified items in their equipment, false otherwise.
 */
fun anyInEquipment(
    player: Player,
    vararg ids: Int,
): Boolean = ids.any { id -> inEquipment(player, id) }

/**
 * Checks if the player has any of the specified items in their inventory.
 *
 * @param player The player whose inventory is being checked.
 * @param ids The IDs of the items to check.
 * @return True if the player has at least one of the specified items in their inventory, false otherwise.
 */
fun anyInInventory(
    player: Player,
    vararg ids: Int,
): Boolean = ids.any { id -> inInventory(player, id) }

/**
 * Returns an item from the player's equipment at a specific slot.
 *
 * @param player The player whose equipment is being checked.
 * @param slot The equipment slot to check.
 * @return The item in the specified equipment slot, or null if the slot is empty.
 */
fun getItemFromEquipment(
    player: Player,
    slot: EquipmentSlot,
): Item? = player.equipment.get(slot.ordinal)

/**
 * Represents an item in a container, with methods to remove or check its existence.
 *
 * @param container The container holding the item.
 * @param itemId The ID of the item in the container.
 */
class ContainerisedItem(
    val container: core.game.container.Container?,
    val itemId: Int,
) {
    /**
     * Removes the item from the container.
     *
     * @return True if the item was removed successfully, false otherwise.
     */
    fun remove(): Boolean = this.container?.remove(this.itemId.asItem()) ?: false

    /**
     * Checks if the item exists in the container.
     *
     * @return True if the item exists in the container, false otherwise.
     */
    fun exists(): Boolean = this.container != null && this.itemId > -1
}

/**
 * Checks if the player has any of the specified items in their inventory, equipment, or bank.
 *
 * @param player The player to check.
 * @param ids The IDs of the items to search for.
 * @return A [ContainerisedItem] representing the item found, or one with a null container if none were found.
 */
fun hasAnItem(
    player: Player,
    vararg ids: Int,
): ContainerisedItem {
    for (searchSpace in arrayOf(player.inventory, player.equipment, player.bank)) {
        for (id in ids) {
            if (searchSpace.containItems(id)) {
                return ContainerisedItem(searchSpace, id)
            }
        }
    }
    return ContainerisedItem(null, -1)
}

/**
 * Checks if the player has any of the valid god items equipped.
 *
 * @param player The player to check.
 * @param god The god to check against.
 * @return True if the player has any of the valid god items equipped, false otherwise.
 */
fun hasGodItem(
    player: Player,
    god: God,
): Boolean {
    god.validItems.forEach { if (inEquipment(player, it)) return true }
    return false
}

/**
 * Determines whether the player should remove certain items, such as the Ring of Wealth.
 *
 * @param player The player to check.
 * @return True if the player should remove certain items, false otherwise.
 */
fun shouldRemoveNothings(player: Player): Boolean {
    val ring = getItemFromEquipment(player, EquipmentSlot.RING)
    return ring != null && ring.id == Items.RING_OF_WEALTH_2572
}

/**
 * Removes an item from the player's specified container.
 *
 * @param player The player removing the item.
 * @param item The item to remove.
 * @param container The container from which to remove the item (defaults to inventory).
 * @return True if the item was removed successfully, false otherwise.
 */
fun <T> removeItem(
    player: Player,
    item: T,
    container: Container = Container.INVENTORY,
): Boolean {
    item ?: return false
    val it =
        when (item) {
            is Item -> item
            is Int -> Item(item)
            else -> throw IllegalStateException("Invalid value passed for item")
        }

    return when (container) {
        Container.INVENTORY -> player.inventory.remove(it)
        Container.BANK -> player.bank.remove(it)
        Container.EQUIPMENT -> player.equipment.remove(it)
        Container.BoB -> {
            if (player.familiarManager.hasFamiliar() && player.familiarManager.familiar.isBurdenBeast)
                (player.familiarManager.familiar as BurdenBeast).container.remove(it)
            else
                false
        }
    }
}

/**
 * Adds an item to the player's specified container.
 *
 * @param player The player receiving the item.
 * @param id The ID of the item to add.
 * @param amount The amount of the item to add (defaults to 1).
 * @param container The container to add the item to (defaults to inventory).
 * @return True if the item was added successfully, false otherwise.
 */
fun addItem(
    player: Player,
    id: Int,
    amount: Int = 1,
    container: Container = Container.INVENTORY,
): Boolean {
    val cont =
        when (container) {
            Container.INVENTORY -> player.inventory
            Container.BANK -> player.bank
            Container.EQUIPMENT -> player.equipment
            Container.BoB -> {
                if(player.familiarManager.hasFamiliar() && player.familiarManager.familiar.isBurdenBeast)
                    (player.familiarManager.familiar as BurdenBeast).container
                else
                    return false
            }
        }

    return cont.add(Item(id, amount))
}

/**
 * Attempts to add an item to the player's inventory or bank, and places it on the ground if both are full.
 *
 * @param player The player receiving the item.
 * @param id The ID of the item to add.
 * @param amount The amount of the item to add (defaults to 1).
 */
fun addItemOrBank(
    player: Player,
    id: Int,
    amount: Int = 1,
) {
    val item = Item(id, amount)
    if (!player.inventory.add(item)) {
        if (player.bank.add(item)) {
            sendMessage(player, colorize("%RThe ${item.name} has been sent to your bank."))
        } else {
            GroundItemManager.create(item, player)
            sendMessage(
                player,
                colorize(
                    "%RAs your inventory and bank account(s) are all full, the ${item.name} has been placed on the ground under your feet. Don't forget to grab it.",
                ),
            )
        }
    }
}

/**
 * Replaces an item in a specified slot of the player's inventory, equipment, or bank, and logs a dupe attempt if necessary.
 *
 * @param player The player whose item is being replaced.
 * @param slot The slot to replace the item in.
 * @param item The new item to add.
 * @param currentItem The current item to replace.
 * @param container The container to perform the replacement in (defaults to inventory).
 * @return The replaced item, or null if the replacement failed.
 */
fun replaceSlot(player: Player, slot: Int, item: Item, currentItem: Item? = null, container: Container = Container.INVENTORY): Item? {
    val cont = when (container) {
        Container.INVENTORY -> player.inventory
        Container.EQUIPMENT -> player.equipment
        Container.BANK -> player.bank
        Container.BoB -> (player.familiarManager.familiar as BurdenBeast).container
    }

    if (item.id == 65535 || item.amount <= 0) {
        return cont.replace(null, slot)
    }

    if (currentItem == null) {
        return cont.replace(item, slot)
    }

    if (cont.remove(currentItem, slot, true)) {
        return cont.replace(item, slot)
    }

    PlayerMonitor.log(player, LogType.DUPE_ALERT, "Potential slot-replacement-based dupe attempt, slot: $slot, item: $item")
    val other = when (container) {
        Container.INVENTORY -> Container.EQUIPMENT
        else -> Container.INVENTORY
    }
    if (removeItem(player, currentItem, other))
        return cont.replace(item, slot)
    return null
}

/**
 * Attempts to add an item to the player's inventory or drops it if there is no room.
 *
 * @param player The player receiving the item.
 * @param id The ID of the item to add.
 * @param amount The amount of the item to add (defaults to 1).
 */
fun addItemOrDrop(
    player: Player,
    id: Int,
    amount: Int = 1,
) {
    val item = Item(id, amount)
    if (amount == 1 || item.definition.isStackable()) {
        if (!player.inventory.add(item)) GroundItemManager.create(item, player)
    } else {
        val singleItem = Item(id, 1)
        for (i in 0 until amount) {
            if (!player.inventory.add(singleItem)) GroundItemManager.create(singleItem, player)
        }
    }
}

/**
 * Makes the NPC invisible and plays a graphics effect, then clears the NPC after a few ticks.
 *
 * @param npc The NPC to make invisible and clear.
 */
fun poofClear(npc: NPC) {
    submitWorldPulse(
        object : Pulse() {
            var counter = 0

            override fun pulse(): Boolean {
                when (counter++) {
                    2 -> {
                        npc.isInvisible = true
                        Graphics.send(
                            Graphics(86),
                            npc.location,
                        )
                    }

                    3 -> npc.clear().also { return true }
                }
                return false
            }
        },
    )
}

/**
 * Returns the number of free slots in the player's inventory.
 *
 * @param player The player whose inventory is being checked.
 * @return The number of free slots in the player's inventory.
 */
fun freeSlots(player: Player): Int = player.inventory.freeSlots()

/**
 * Returns an animation corresponding to the given ID.
 *
 * @param id The ID of the animation.
 * @return The animation object corresponding to the specified ID.
 */
fun getAnimation(id: Int): Animation = Animation(id)

/**
 * Returns an animation corresponding to the given ID, with priority.
 *
 * @param id The ID of the animation.
 * @param priority The priority level of the animation.
 * @return The animation object corresponding to the specified ID with priority.
 */
fun getAnimationWithPriority(
    id: Int,
    priority: Animator.Priority,
): Animation = Animation(id, Animator.Priority.values()[priority.ordinal])

/**
 * Resets the player's animation to a neutral state.
 *
 * @param player The player whose animation is being reset.
 */
fun resetAnimator(player: Player) {
    player.animator.animate(Animation(-1, Animator.Priority.VERY_HIGH))
}

/**
 * Returns the duration of an animation in ticks.
 *
 * @param animation The animation object.
 * @return The duration of the animation in ticks.
 */
fun animationDuration(animation: Animation): Int = animation.definition.getDurationTicks()

/**
 * Returns the number of cycles for a specific animation.
 *
 * @param animation The ID of the animation.
 * @return The number of cycles the animation takes.
 */
fun animationCycles(animation: Int): Int {
    val def = AnimationDefinition.forId(animation)
    return def!!.getCycles()
}

/**
 * Rewards the player with experience in a specified skill.
 *
 * @param player The player to reward.
 * @param skill The skill to reward experience in.
 * @param amount The amount of experience to reward.
 */
fun rewardXP(
    player: Player,
    skill: Int,
    amount: Double,
) {
    player.skills.addExperience(skill, amount)
}

/**
 * Replaces a scenery object with another for a specified duration or permanently.
 *
 * @param toReplace The scenery object to replace.
 * @param with The ID of the new scenery object.
 * @param forTicks The number of ticks the replacement should last, or -1 for permanent replacement.
 * @param loc The location to replace the scenery at, or null to use the current location of the scenery.
 */
fun replaceScenery(
    toReplace: Scenery,
    with: Int,
    forTicks: Int,
    loc: Location? = null,
) {
    val newLoc =
        when (loc) {
            null -> toReplace.location
            else -> loc
        }
    if (forTicks == -1) {
        SceneryBuilder.replace(toReplace, toReplace.transform(with, toReplace.rotation, newLoc))
    } else {
        SceneryBuilder.replace(toReplace, toReplace.transform(with, toReplace.rotation, newLoc), forTicks)
    }
    toReplace.isActive = false
}

/**
 * Adds a scenery object to the game world.
 *
 * @param scenery The scenery object to be added.
 */
fun addScenery(scenery: Scenery) {
    SceneryBuilder.add(scenery)
}

/**
 * Creates and adds a scenery object to the game world at the specified location.
 *
 * @param sceneryId The ID of the scenery to be added.
 * @param location The location where the scenery should be placed.
 * @param rotation The rotation of the scenery. Default is 0.
 * @param type The type of scenery. Default is 22.
 * @return The created scenery object.
 */
fun addScenery(
    sceneryId: Int,
    location: Location,
    rotation: Int = 0,
    type: Int = 22,
): Scenery {
    val scenery = Scenery(sceneryId, location, type, rotation)
    SceneryBuilder.add(scenery)
    return scenery
}

/**
 * Removes a scenery object from the game world.
 *
 * @param scenery The scenery object to be removed.
 */
fun removeScenery(scenery: Scenery) {
    SceneryBuilder.remove(scenery)
}

/**
 * Replaces one scenery object with another in the game world for a specified duration.
 *
 * @param toReplace The scenery object to be replaced.
 * @param with The ID of the scenery to replace with.
 * @param forTicks The duration (in ticks) to replace the scenery. Use -1 for permanent replacement.
 * @param rotation The direction to rotate the new scenery to.
 * @param loc The location of the new scenery. Default is the location of the original scenery.
 */
fun replaceScenery(
    toReplace: Scenery,
    with: Int,
    forTicks: Int,
    rotation: Direction,
    loc: Location? = null,
) {
    val newLoc =
        when (loc) {
            null -> toReplace.location
            else -> loc
        }
    val rot =
        when (rotation) {
            Direction.NORTH_WEST -> 0
            Direction.NORTH -> 1
            Direction.NORTH_EAST -> 2
            Direction.EAST -> 4
            Direction.SOUTH_EAST -> 7
            Direction.SOUTH -> 6
            Direction.SOUTH_WEST -> 5
            Direction.WEST -> 3
        }
    if (forTicks == -1) {
        SceneryBuilder.replace(toReplace, toReplace.transform(with, rot, newLoc))
    } else {
        SceneryBuilder.replace(toReplace, toReplace.transform(with, rot, newLoc), forTicks)
    }
    toReplace.isActive = false
}

/**
 * Gets the name of an item given its ID.
 *
 * @param id The ID of the item.
 * @return The name of the item.
 */
fun getItemName(id: Int): String = ItemDefinition.forId(id).name

/**
 * Checks if the player has space in their inventory for a item.
 *
 * @param player The player to check.
 * @param item The item to check space for.
 * @return True if there is space for the item in the player's inventory.
 */
fun hasSpaceFor(
    player: Player,
    item: Item,
): Boolean = player.inventory.hasSpaceFor(item)

/**
 * Gets the current number of world ticks.
 *
 * @return The current world tick count.
 */
fun getWorldTicks(): Int = GameWorld.ticks

/**
 * Creates an audio object with the id and other parameters.
 *
 * @param id The ID of the audio.
 * @param volume The volume of the audio. Default is 10.
 * @param delay The delay before the audio plays. Default is 1.
 * @return The created Audio object.
 */
fun getAudio(
    id: Int,
    volume: Int = 10,
    delay: Int = 1,
): Audio = Audio(id, volume, delay)

/**
 * Plays a jingle for a player.
 *
 * @param player The player to play the jingle for.
 * @param jingleId The ID of the jingle to be played.
 */
fun playJingle(
    player: Player,
    jingleId: Int,
) {
    PacketRepository.send(MusicPacket::class.java, MusicContext(player, jingleId, true))
}

/**
 * Applies an impact to an entity, affecting its health or other attributes.
 *
 * @param entity The entity to apply the impact to.
 * @param amount The amount of damage or impact.
 * @param type The type of hitsplat (impact effect) to apply.
 */
fun impact(
    entity: Entity,
    amount: Int,
    type: ImpactHandler.HitsplatType = ImpactHandler.HitsplatType.NORMAL,
) {
    entity.impactHandler.manualHit(entity, amount, type)
}

/**
 * Applies an impact to an entity, affecting its health or other attributes.
 *
 * @param entity The entity to apply the impact to.
 * @param amount The amount of damage or impact.
 * @param type The type of hitsplat (impact effect) to apply.
 * @param ticks The delay in ticks before the damage punch.
 */
fun impact(
    entity: Entity,
    amount: Int,
    type: ImpactHandler.HitsplatType = ImpactHandler.HitsplatType.NORMAL,
    ticks: Int,
) {
    entity.impactHandler.manualHit(entity, amount, type, ticks)
}

/**
 * Checks if a specific node (NPC, Scenery, or Item) has a particular option available.
 *
 * @param node The node to check.
 * @param option The option to check for.
 * @return True if the option exists on the node.
 * @throws IllegalArgumentException If the node is not of a valid type.
 */
fun hasOption(
    node: Node,
    option: String,
): Boolean =
    when (node) {
        is NPC -> node.definition.hasAction(option)
        is Scenery -> node.definition.hasAction(option)
        is Item -> node.definition.hasAction(option)
        else -> throw IllegalArgumentException("Expected an NPC, Scenery or an Item, got ${node.javaClass.simpleName}.")
    }

/**
 * Animates a scenery object.
 *
 * @param player The player to animate the scenery for.
 * @param obj The scenery object to animate.
 * @param animationId The ID of the animation to apply.
 * @param global If true, the animation is visible to all players. Default is false.
 */
fun animateScenery(
    player: Player,
    obj: Scenery,
    animationId: Int,
    global: Boolean = false,
) {
    player.packetDispatch.sendSceneryAnimation(obj, getAnimation(animationId), global)
}

/**
 * Animates a scenery object.
 *
 * @param obj The scenery object to animate.
 * @param animationId The ID of the animation to apply.
 */
fun animateScenery(
    obj: Scenery,
    animationId: Int,
) {
    val animation = Animation(animationId)
    animation.setObject(obj)
    getRegionChunk(obj.location).flag(
        AnimateObjectUpdateFlag(
            animation,
        ),
    )
}

/**
 * Spawns a projectile between two entities.
 *
 * @param source The source entity that fires the projectile.
 * @param dest The target entity to hit with the projectile.
 * @param projectileId The ID of the projectile.
 */
fun spawnProjectile(
    source: Entity,
    dest: Entity,
    projectileId: Int,
) {
    Projectile.create(source, dest, projectileId).send()
}

/**
 * Spawns a projectile from a source location to a destination location.
 *
 * @param source The source location of the projectile.
 * @param dest The destination location of the projectile.
 * @param projectile The ID of the projectile.
 * @param startHeight The starting height of the projectile.
 * @param endHeight The end height of the projectile.
 * @param delay The delay before the projectile is fired.
 * @param speed The speed of the projectile.
 * @param angle The angle at which the projectile travels.
 */
fun spawnProjectile(
    source: Location,
    dest: Location,
    projectile: Int,
    startHeight: Int,
    endHeight: Int,
    delay: Int,
    speed: Int,
    angle: Int,
) {
    Projectile
        .create(
            source,
            dest,
            projectile,
            startHeight,
            endHeight,
            delay,
            speed,
            angle,
            source.getDistance(dest).toInt(),
        ).send()
}

/**
 * Faces an entity towards a specific node (NPC or Location) for a specified duration.
 *
 * @param entity The entity to face.
 * @param toFace The node (NPC or Location) to face.
 * @param duration The duration (in ticks) to face the node. Use -1 for indefinite facing.
 */
fun face(
    entity: Entity,
    toFace: Node,
    duration: Int = -1,
) {
    if (duration == -1) {
        when (toFace) {
            is Location -> entity.faceLocation(toFace)
            is Entity -> entity.face(toFace)
        }
    } else {
        when (toFace) {
            is Location -> entity.faceTemporary(toFace.asNpc(), duration)
            else -> entity.faceTemporary(toFace as Entity, duration)
        }
    }
}

/**
 * Resets the entity's facing direction to its original location.
 *
 * @param entity The entity whose facing direction will be reset.
 */
fun resetFace(entity: Entity) {
    entity.face(null)
    entity.faceLocation(entity.location)
}

/**
 * Faces an entity towards a specified location.
 *
 * @param entity The entity to face.
 * @param location The location to face towards.
 */
fun faceLocation(
    entity: Entity,
    location: Location,
) {
    entity.faceLocation(location)
}

/**
 * Opens an interface for a player.
 *
 * @param player The player to open the interface for.
 * @param id The ID of the interface to open.
 */
fun openInterface(
    player: Player,
    id: Int,
) {
    player.interfaceManager.open(Component(id))
}

/**
 * Opens an overlay for a player.
 *
 * @param player The player to open the overlay for.
 * @param id The ID of the overlay to open.
 */
fun openOverlay(
    player: Player,
    id: Int,
) {
    player.interfaceManager.openOverlay(Component(id))
}

/**
 * Opens a chatbox for a player.
 *
 * @param player The player to open the chatbox for.
 * @param id The ID of the chatbox to open.
 */
fun openChatbox(
    player: Player,
    id: Int,
) {
    player.interfaceManager.openChatbox(Component(id))
}

/**
 * Closes the overlay for a player.
 *
 * @param player The player whose overlay will be closed.
 */
fun closeOverlay(player: Player) {
    player.interfaceManager.closeOverlay()
}

/**
 * Closes all interfaces for the given player.
 *
 * @param player The player whose interfaces will be closed.
 */
fun closeAllInterfaces(player: Player) {
    player.interfaceManager.close()
    player.interfaceManager.closeChatbox()
    player.dialogueInterpreter.close()
}

/**
 * Sends a message to the player, splitting it into lines of up to 86 characters.
 *
 * @param player The player to send the message to.
 * @param message The message to send.
 */
fun sendMessage(
    player: Player,
    message: String,
    ticks: Int? = null
) {
    val lines = splitLines(message, 86)
    if (ticks != null) {
        player.sendMessages(ticks, *lines)
    } else {
        player.sendMessages(*splitLines(message, 86))
    }
}

/**
 * Sends multiple messages to the player.
 *
 * @param player The player to send the messages to.
 * @param message The messages to send.
 */
fun sendMessages(
    player: Player,
    vararg message: String,
) {
    player.packetDispatch.sendMessages(*message)
}

/**
 * Sends a chat message to the entity, optionally with a delay.
 *
 * @param entity The entity to send the chat message to.
 * @param message The message to send.
 * @param delay The delay in ticks before sending the message. Defaults to -1 (no delay).
 */
fun sendChat(
    entity: Entity,
    message: String,
    delay: Int = -1,
) {
    if (delay > -1) {
        queueScript(entity, delay, QueueStrength.SOFT) {
            entity.sendChat(message)
            return@queueScript stopExecuting(entity)
        }
    } else {
        entity.sendChat(message)
    }
}

/**
 * Sends a dialogue message to the player, splitting it into lines if necessary.
 *
 * @param player The player to send the dialogue message to.
 * @param message The dialogue message.
 */
fun sendDialogue(
    player: Player,
    message: String,
) {
    player.dialogueInterpreter.sendDialogue(*splitLines(message))
}

/**
 * Sends a dialogue for destroying an item, with an optional message.
 *
 * @param player The player to send the dialogue to.
 * @param item The ID of the item to be destroyed.
 * @param message An optional message to include in the dialogue.
 */
fun sendDestroyItemDialogue(
    player: Player,
    item: Int,
    message: String?,
) {
    player.dialogueInterpreter.sendDestroyItem(item, message)
}

/**
 * Sends multiple lines of dialogue to the player.
 *
 * @param player The player to send the dialogue to.
 * @param message The dialogue lines.
 */
fun sendDialogueLines(
    player: Player,
    vararg message: String,
) {
    player.dialogueInterpreter.sendDialogue(*message)
}

/**
 * Sends a set of dialogue options to the player.
 *
 * @param player The player to send the options to.
 * @param title The title of the options dialogue.
 * @param options The options available to the player.
 */
fun sendOptions(
    player: Player,
    title: String,
    vararg options: String,
) {
    player.dialogueInterpreter.sendOptions(title, *options)
}

/**
 * Sends a plain dialogue message to the player, with an option to hide the continue button.
 *
 * @param player The player to send the dialogue to.
 * @param hideContinue Whether to hide the continue button. Defaults to false.
 * @param message The dialogue message.
 */
fun sendPlainDialogue(
    player: Player,
    hideContinue: Boolean = false,
    vararg message: String,
) {
    player.dialogueInterpreter.sendPlainMessage(hideContinue, *message)
}

/**
 * Sends a plain dialogue message to the player, with an option to hide the continue button.
 *
 * @param player The player to send the dialogue to.
 * @param hideContinue Whether to hide the continue button. Defaults to false.
 * @param message The dialogue message.
 */
fun sendPlainDialogue(
    player: Player,
    hideContinue: Boolean = false,
    message: String,
) {
    player.dialogueInterpreter.sendPlainMessage(hideContinue, *splitLines(message))
}

/**
 * Sends a plain dialogue message that cannot be closed, with an option to hide the continue button.
 *
 * @param player The player to send the dialogue to.
 * @param hideContinue Whether to hide the continue button. Defaults to false.
 * @param message The dialogue message.
 */
fun sendUnclosablePlainDialogue(
    player: Player,
    hideContinue: Boolean = false,
    vararg message: String,
) {
    Component.setUnclosable(player, player.dialogueInterpreter.sendPlainMessage(hideContinue, *message))
}

/**
 * Animates an entity using the provided animation, with an option to force the animation.
 *
 * @param entity The entity to animate.
 * @param anim The animation to apply, either as an Int (ID) or Animation object.
 * @param forced Whether to force the animation, overriding any existing ones. Defaults to false.
 */
fun <T> animate(
    entity: Entity,
    anim: T,
    forced: Boolean = false,
) {
    val animation =
        when (anim) {
            is Int -> Animation(anim)
            is Animation -> anim
            else -> throw IllegalStateException("Invalid value passed for anim")
        }

    if (forced) {
        entity.animator.forceAnimation(animation)
    } else {
        entity.animator.animate(animation)
    }
}

/**
 * Sends an animation to the player with a specified delay.
 *
 * @param player The player to send the animation to.
 * @param animation The animation ID.
 * @param delay The delay in ticks before the animation is sent.
 */
fun sendAnimation(
    player: Player,
    animation: Int,
    delay: Int,
) {
    player.packetDispatch.sendAnimation(animation, delay)
}

/**
 * Plays an audio file for the player, with optional delay, loops, and location parameters.
 *
 * @param player The player to play the audio for.
 * @param id The audio file ID.
 * @param delay The delay in ticks before the audio starts. Defaults to 0.
 * @param loops The number of times the audio should loop. Defaults to 1.
 * @param location The location to play the audio from. Defaults to null.
 * @param radius The radius within which the audio is heard. Defaults to Audio.defaultAudioRadius.
 */
@JvmOverloads
fun playAudio(player: Player, id: Int, delay: Int = 0, loops: Int = 1, location: Location? = null, radius: Int = Audio.defaultAudioRadius) {
    PacketRepository.send(AudioPacket::class.java, DefaultContext(player, Audio(id, delay, loops, radius), location))
}

/**
 * Plays an audio file for all players within a specified radius of a given location.
 *
 * @param location The location where the audio will be played from.
 * @param id The audio file ID.
 * @param delay The delay in ticks before the audio starts. Defaults to 0.
 * @param loops The number of times the audio should loop. Defaults to 1.
 * @param radius The radius within which players will hear the audio. Defaults to Audio.defaultAudioRadius.
 */
@JvmOverloads
fun playGlobalAudio(location: Location, id: Int, delay: Int = 0, loops: Int = 1, radius: Int = Audio.defaultAudioRadius) {
    val nearbyPlayers = RegionManager.getLocalPlayers(location, radius)
    for (player in nearbyPlayers) {
        PacketRepository.send(AudioPacket::class.java, DefaultContext(player, Audio(id, delay, loops, radius), location))
    }
}

/**
 * Plays a hurt audio sound for the player, selecting the appropriate sound based on their gender.
 *
 * @param player The player to play the hurt audio for.
 * @param delay The delay in ticks before the audio starts. Defaults to 0.
 */
fun playHurtAudio(
    player: Player,
    delay: Int = 0,
) {
    val maleHurtAudio = intArrayOf(Sounds.HUMAN_HIT4_516, Sounds.HUMAN_HIT5_517, Sounds.HUMAN_HIT_518, Sounds.HUMAN_HIT_6_522)
    val femaleHurtAudio = intArrayOf(Sounds.FEMALE_HIT_506, Sounds.FEMALE_HIT_507, Sounds.FEMALE_HIT2_508, Sounds.FEMALE_HIT_2_510)
    if (player.isMale) {
        playAudio(player, maleHurtAudio.random(), delay)
    } else {
        playAudio(player, femaleHurtAudio.random(), delay)
    }
}

/**
 * Opens a dialogue for the player, using various types of dialogue objects.
 *
 * @param player The player to open the dialogue for.
 * @param dialogue The dialogue to open, either as an ID, String, DialogueFile, or SkillDialogueHandler.
 * @param args Any additional arguments to pass to the dialogue.
 */
fun openDialogue(player: Player, dialogue: Any, vararg args: Any) {
    player.dialogueInterpreter.close()
    when (dialogue) {
        is Int -> player.dialogueInterpreter.open(dialogue, *args)
        is String -> player.dialogueInterpreter.open(getDialogueKey(dialogue), *args)
        is DialogueFile -> player.dialogueInterpreter.open(dialogue, *args)
        is SkillDialogueHandler -> dialogue.open()
        else -> log(ContentAPI::class.java, Log.ERR, "Invalid object type passed to openDialogue() -> ${dialogue.javaClass.simpleName}")
    }
}

/**
 * Finds an NPC by its ID.
 *
 * @param id The ID of the NPC to find.
 * @return The NPC with the specified ID, or null if not found.
 */
fun findNPC(id: Int): NPC? = Repository.findNPC(id)

/**
 * Gets the scenery at the specified location.
 *
 * @param x The x-coordinate of the location.
 * @param y The y-coordinate of the location.
 * @param z The z-coordinate (height level) of the location.
 * @return The Scenery object at the specified location, or null if not found.
 */
fun getScenery(
    x: Int,
    y: Int,
    z: Int,
): Scenery? = RegionManager.getObject(z, x, y)

/**
 * Gets the scenery at the specified location.
 *
 * @param loc The location.
 * @return The Scenery object at the location, or null if not found.
 */
fun getScenery(loc: Location): Scenery? = RegionManager.getObject(loc)

/**
 * Finds an NPC by location and ID.
 *
 * @param refLoc The reference location.
 * @param id The ID of the NPC to find.
 * @return The NPC with the specified ID at the given location, or null if not found.
 */
fun findNPC(
    refLoc: Location,
    id: Int,
): NPC? = Repository.npcs.firstOrNull { it.id == id && it.location.withinDistance(refLoc) }

/**
 * Finds a local NPC by its ID for the specified entity.
 *
 * @param entity The entity to check local NPCs for.
 * @param id The ID of the NPC to find.
 * @return The NPC with the specified ID, or null if not found.
 */
fun findLocalNPC(
    entity: Entity,
    id: Int,
): NPC? = RegionManager.getLocalNpcs(entity).firstOrNull { it.id == id }

/**
 * Finds local NPCs within a specified distance of a location.
 *
 * @param location The location to check for nearby NPCs.
 * @param distance The maximum distance from the location to check.
 * @return A list of NPCs within the specified distance of the location.
 */
fun findLocalNPCs(
    location: Location,
    distance: Int,
): MutableList<NPC> = RegionManager.getLocalNpcs(location, distance)

/**
 * Finds local NPCs by their IDs for the specified entity.
 *
 * @param entity The entity to check local NPCs for.
 * @param ids The IDs of the NPCs to find.
 * @return A list of NPCs with the specified IDs.
 */
fun findLocalNPCs(
    entity: Entity,
    ids: IntArray,
): List<NPC> = RegionManager.getLocalNpcs(entity).filter { it.id in ids }.toList()

/**
 * Finds local NPCs by their IDs for the specified entity and within a certain distance.
 *
 * @param entity The entity to check local NPCs for.
 * @param ids The IDs of the NPCs to find.
 * @param distance The maximum distance from the entity to check.
 * @return A list of NPCs with the specified IDs within the specified distance.
 */
fun findLocalNPCs(
    entity: Entity,
    ids: IntArray,
    distance: Int,
): List<NPC> = RegionManager.getLocalNpcs(entity, distance).filter { it.id in ids }.toList()

/**
 * Gets the zone borders for a specified region ID.
 *
 * @param regionId The ID of the region.
 * @return The zone borders for the specified region.
 */
fun getRegionBorders(regionId: Int): ZoneBorders = ZoneBorders.forRegion(regionId)

/**
 * Retrieves an attribute from the entity, providing a default value if not found.
 *
 * @param entity The entity from which to get the attribute.
 * @param attribute The name of the attribute.
 * @param default The default value to return if the attribute is not found.
 * @return The value of the attribute, or the default if not found.
 */
fun <T> getAttribute(
    entity: Entity,
    attribute: String,
    default: T,
): T = entity.getAttribute(attribute, default)

/**
 * Sets an attribute on the entity.
 *
 * @param entity The entity to set the attribute for.
 * @param attribute The name of the attribute.
 * @param value The value to set for the attribute.
 */
fun <T> setAttribute(
    entity: Entity,
    attribute: String,
    value: T,
) {
    entity.setAttribute(attribute, value)
}

/**
 * Removes an attribute from the entity.
 *
 * @param entity The entity to remove the attribute from.
 * @param attribute The name of the attribute to remove.
 */
fun removeAttribute(
    entity: Entity,
    attribute: String,
) {
    entity.removeAttribute(attribute.replace("/save:", ""))
}

/**
 * Removes multiple attributes from the entity.
 *
 * @param entity The entity to remove the attributes from.
 * @param attributes The attributes to remove.
 */
fun removeAttributes(
    entity: Entity,
    vararg attributes: String,
) {
    for (attribute in attributes) removeAttribute(entity, attribute)
}

/**
 * Registers a timer for the specified entity.
 *
 * @param entity The entity to register the timer for.
 * @param timer The timer to register.
 */
fun registerTimer(
    entity: Entity,
    timer: RSTimer?,
) {
    if (timer == null) return
    entity.timers.registerTimer(timer)
}

/**
 * Retrieves or starts a timer for the specified entity.
 *
 * @param entity The entity to retrieve or start the timer for.
 * @param args Any arguments required to start the timer.
 * @return The existing or newly spawned timer.
 */
inline fun <reified T : RSTimer> getOrStartTimer(
    entity: Entity,
    vararg args: Any,
): T {
    val existing = getTimer<T>(entity)
    if (existing != null) return existing
    return spawnTimer<T>(*args).also { registerTimer(entity, it) }
}

/**
 * Spawns a new timer with the specified identifier and arguments.
 *
 * @param identifier The identifier of the timer.
 * @param args The arguments to spawn the timer with.
 * @return The spawned timer, or null if the timer could not be spawned.
 */
fun spawnTimer(
    identifier: String,
    vararg args: Any,
): RSTimer? = TimerRegistry.getTimerInstance(identifier, *args)

/**
 * Spawns a new timer of the specified type with the given arguments.
 *
 * @param args The arguments to spawn the timer with.
 * @return The spawned timer.
 */
inline fun <reified T : RSTimer> spawnTimer(vararg args: Any): T = TimerRegistry.getTimerInstance<T>(*args)!!

/**
 * Checks if a timer of the specified type is active for the given entity.
 *
 * @param entity The entity to check for an active timer.
 * @return True if the timer is active, false otherwise.
 */
inline fun <reified T : RSTimer> hasTimerActive(entity: Entity): Boolean = getTimer<T>(entity) != null

/**
 * Retrieves the active timer of the specified type for the entity.
 *
 * @param entity The entity to retrieve the timer from.
 * @return The active timer, or null if not found.
 */
inline fun <reified T : RSTimer> getTimer(entity: Entity): T? = entity.timers.getTimer<T>()

/**
 * Removes the active timer of the specified type for the given entity.
 *
 * @param entity The entity to remove the timer from.
 */
inline fun <reified T : RSTimer> removeTimer(entity: Entity) {
    entity.timers.removeTimer<T>()
}

/**
 * Checks if a timer with the specified identifier is active for the given entity.
 *
 * @param entity The entity to check for an active timer.
 * @param identifier The identifier of the timer.
 * @return True if the timer is active, false otherwise.
 */
fun hasTimerActive(
    entity: Entity,
    identifier: String,
): Boolean = getTimer(entity, identifier) != null

/**
 * Retrieves the timer associated with the given identifier for the specified entity.
 *
 * @param entity The entity whose timer is to be retrieved.
 * @param identifier The unique identifier for the timer.
 * @return The timer associated with the identifier, or null if not found.
 */
fun getTimer(
    entity: Entity,
    identifier: String,
): RSTimer? = entity.timers.getTimer(identifier)

/**
 * Removes the timer associated with the given identifier from the specified entity.
 *
 * @param entity The entity whose timer is to be removed.
 * @param identifier The unique identifier for the timer to be removed.
 */
fun removeTimer(
    entity: Entity,
    identifier: String,
) {
    entity.timers.removeTimer(identifier)
}

/**
 * Removes the specified timer from the entity.
 *
 * @param entity The entity from which the timer is to be removed.
 * @param timer The timer to be removed.
 */
fun removeTimer(
    entity: Entity,
    timer: RSTimer,
) {
    entity.timers.removeTimer(timer)
}

/**
 * Locks the entity for a specified duration, preventing actions like movement or interaction.
 *
 * @param entity The entity to be locked.
 * @param duration The duration (in seconds) for which the entity will be locked.
 */
fun lock(
    entity: Entity,
    duration: Int,
) {
    entity.lock(duration)
}

/**
 * Locks the entity's ability to interact with others for a specified duration.
 *
 * @param entity The entity whose interactions are to be locked.
 * @param duration The duration (in seconds) for which interactions are locked.
 */
fun lockInteractions(
    entity: Entity,
    duration: Int,
) {
    entity.locks.lockInteractions(duration)
}

/**
 * Locks the entity's movement for a specified duration.
 *
 * @param entity The entity whose movement is to be locked.
 * @param duration The duration (in seconds) for which movement is locked.
 */
fun lockMovement(
    entity: Entity,
    duration: Int,
) {
    entity.locks.lockMovement(duration)
}

/**
 * Locks the entity's equipment for a specified duration.
 *
 * @param entity The entity whose equipment is to be locked.
 * @param duration The duration (in seconds) for which equipment is locked.
 */
fun lockEquipment(
    entity: Entity,
    duration: Int,
) {
    entity.locks.equipmentLock!!.lock(duration)
}

/**
 * Checks if the entity's teleportation is currently locked.
 *
 * @param entity The entity whose teleportation lock status is to be checked.
 * @return `true` if teleportation is locked, `false` otherwise.
 */
fun lockTeleport(entity: Entity) {
    entity.locks.isTeleportLocked()
}

/**
 * Locks the entity's teleportation for a specified number of ticks.
 *
 * @param entity The entity whose teleportation is to be locked.
 * @param ticks The number of ticks for which teleportation is locked.
 */
fun lockTeleport(
    entity: Entity,
    ticks: Int,
) {
    entity.locks.lockTeleport(ticks)
}

/**
 * Unlocks the entity, allowing them to perform actions such as movement and interactions.
 *
 * @param entity The entity to be unlocked.
 */
fun unlock(entity: Entity) {
    entity.unlock()
}

/**
 * Creates a location with the specified coordinates.
 *
 * @param x The x-coordinate of the location.
 * @param y The y-coordinate of the location.
 * @param z The z-coordinate of the location.
 * @return A new location object.
 */
fun location(
    x: Int,
    y: Int,
    z: Int,
): Location = Location.create(x, y, z)

/**
 * Checks if the entity is within the boundaries of a specified zone.
 *
 * @param entity The entity whose position is to be checked.
 * @param borders The borders of the zone to check.
 * @return `true` if the entity is inside the borders, `false` otherwise.
 */
fun inBorders(
    entity: Entity,
    borders: ZoneBorders,
): Boolean = borders.insideBorder(entity)

/**
 * Checks if the entity is within a zone defined by its southwest and northeast coordinates.
 *
 * @param entity The entity whose position is to be checked.
 * @param swX The x-coordinate of the southwest corner.
 * @param swY The y-coordinate of the southwest corner.
 * @param neX The x-coordinate of the northeast corner.
 * @param neY The y-coordinate of the northeast corner.
 * @return `true` if the entity is inside the specified zone, `false` otherwise.
 */
fun inBorders(
    entity: Entity,
    swX: Int,
    swY: Int,
    neX: Int,
    neY: Int,
): Boolean = ZoneBorders(swX, swY, neX, neY).insideBorder(entity)

/**
 * Checks if the entity is within a named zone.
 *
 * @param entity The entity whose position is to be checked.
 * @param name The name of the zone.
 * @return `true` if the entity is inside the specified zone, `false` otherwise.
 */
fun inZone(
    entity: Entity,
    name: String,
): Boolean = entity.zoneMonitor.isInZone(name)

/**
 * Heals the entity by the specified amount.
 *
 * @param entity The entity to be healed.
 * @param amount The amount of health to heal.
 */
fun heal(
    entity: Entity,
    amount: Int,
) {
    entity.skills.heal(amount)
}

/**
 * Retrieves the value of a varp for the player.
 *
 * @param player The player whose varp value is to be retrieved.
 * @param varpIndex The index of the varp.
 * @return The value of the varp at the specified index.
 */
fun getVarp(
    player: Player,
    varpIndex: Int,
): Int = player.varpMap[varpIndex] ?: 0

/**
 * Retrieves the value of a varbit for the player based on the provided varbit definition.
 *
 * @param player The player whose varbit value is to be retrieved.
 * @param def The varbit definition containing the necessary details.
 * @return The value of the varbit.
 */
fun getVarbit(
    player: Player,
    def: VarbitDefinition,
): Int {
    val mask = def.mask
    val current = getVarp(player, def.varpId)
    return (current shr def.startBit) and mask
}

/**
 * Retrieves the value of a varbit for the player based on the provided varbit ID.
 *
 * @param player The player whose varbit value is to be retrieved.
 * @param varbitId The ID of the varbit.
 * @return The value of the varbit.
 */
fun getVarbit(
    player: Player,
    varbitId: Int,
): Int {
    val def = VarbitDefinition.forId(varbitId)
    return getVarbit(player, def)
}

/**
 * Sets the value of a varp for the player.
 *
 * @param player The player whose varp value is to be set.
 * @param varpIndex The index of the varp to be set.
 * @param value The value to set for the varp.
 * @param save If `true`, the varp value will be saved.
 */
@JvmOverloads
fun setVarp(
    player: Player,
    varpIndex: Int,
    value: Int,
    save: Boolean = false,
) {
    player.varpMap[varpIndex] = value
    player.saveVarp[varpIndex] = save
    player.packetDispatch.sendVarp(varpIndex, value)
}

/**
 * Marks a varp to be saved for the player.
 *
 * @param player The player whose varp is to be saved.
 * @param varpIndex The index of the varp to be saved.
 */
fun saveVarp(
    player: Player,
    varpIndex: Int,
) {
    player.saveVarp[varpIndex] = true
}

/**
 * Unmarks a varp from being saved for the player.
 *
 * @param player The player whose varp is to be unsaved.
 * @param varpIndex The index of the varp to be unsaved.
 */
fun unsaveVarp(
    player: Player,
    varpIndex: Int,
) {
    player.saveVarp.remove(varpIndex)
}

/**
 * Sets the value of a varbit for the player based on the provided varbit definition.
 *
 * @param player The player whose varbit value is to be set.
 * @param def The varbit definition containing the necessary details.
 * @param value The value to set for the varbit.
 * @param save If `true`, the varbit value will be saved.
 */
@JvmOverloads
fun setVarbit(
    player: Player,
    def: VarbitDefinition,
    value: Int,
    save: Boolean = false,
) {
    val mask = def.mask
    val current = getVarp(player, def.varpId) and (mask shl def.startBit).inv()
    val newValue = (value and mask) shl def.startBit
    setVarp(player, def.varpId, current or newValue, save)
}

/**
 * Sets the value of a varbit for the player based on the provided varbit ID.
 *
 * @param player The player whose varbit value is to be set.
 * @param varbitId The ID of the varbit to be set.
 * @param value The value to set for the varbit.
 * @param save If `true`, the varbit value will be saved.
 */
@JvmOverloads
fun setVarbit(
    player: Player,
    varbitId: Int,
    value: Int,
    save: Boolean = false,
) {
    val def = VarbitDefinition.forId(varbitId)

    if (def == null) {
        logWithStack(ContentAPI::class.java, Log.ERR, "Trying to setVarbit $varbitId, which doesn't seem to exist.")
        return
    }

    setVarbit(player, def, value, save)
}

/**
 * Increments the value of a varbit.
 *
 * @param player The player.
 * @param def The varbit definition containing the necessary details.
 * @param amount The amount to increment by (default is 1).
 * @param save If `true`, the varbit value will be saved.
 */
@JvmOverloads
fun incrementVarbit(
    player: Player,
    def: VarbitDefinition,
    amount: Int = 1,
    save: Boolean = false,
) {
    val currentVarp = getVarp(player, def.varpId)
    val currentValue = (currentVarp shr def.startBit) and def.mask
    val newValue = (currentValue + amount).coerceIn(0, def.mask)
    setVarbit(player, def, newValue, save)
}

/**
 * Increments the value of a varbit for the player based on the provided varbit ID.
 *
 * @param player The player whose varbit value is to be incremented.
 * @param varbitId The ID of the varbit to be incremented.
 * @param amount The amount to increment by (default is 1).
 * @param save If `true`, the varbit value will be saved.
 */
@JvmOverloads
fun incrementVarbit(
    player: Player,
    varbitId: Int,
    amount: Int = 1,
    save: Boolean = false,
) {
    val def = VarbitDefinition.forId(varbitId)

    if (def == null) {
        logWithStack(ContentAPI::class.java, Log.ERR, "Trying to incrementVarbit $varbitId, which doesn't seem to exist.")
        return
    }

    incrementVarbit(player, def, amount, save)
}

/**
 * Sets the value of a varc for the player.
 *
 * @param player The player whose varc value is to be set.
 * @param varc The varc to set.
 * @param value The value to set for the varc.
 */
fun setVarc(
    player: Player,
    varc: Int,
    value: Int,
) {
    player.packetDispatch.sendVarcUpdate(varc.toShort(), value)
}

/**
 * Reinitializes the varps for the player, applying any saved values.
 *
 * @param player The player whose varps are to be reinitialized.
 */
fun reinitVarps(player: Player) {
    for ((index, value) in player.varpMap) {
        setVarp(player, index, value, player.saveVarp[index] ?: false)
    }
}

/**
 * Forces the entity to walk to a specified destination using a specified type of pathfinding.
 *
 * @param entity The entity to move.
 * @param dest The destination location.
 * @param type The type of pathfinding to use.
 */
fun forceWalk(
    entity: Entity,
    dest: Location,
    type: String,
) {
    if (type == "clip") {
        ForceMovement(entity, dest, 10, 10).run()
        return
    }
    val pathfinder =
        when (type) {
            "smart" -> Pathfinder.SMART
            else -> Pathfinder.DUMB
        }
    val path = Pathfinder.find(entity, dest, true, pathfinder)
    path.walk(entity)
}

/**
 * Forces the player to move from one location to another with specific arrival times and optional animation.
 *
 * @param player The player to move.
 * @param start The starting location.
 * @param dest The destination location.
 * @param startArrive The time to start the arrival animation (in ticks).
 * @param destArrive The time to arrive at the destination (in ticks).
 * @param dir The direction to move in, or `null` to auto-calculate.
 * @param anim The animation to play while moving.
 * @param callback An optional callback to invoke once the player has arrived.
 */
fun forceMove(
    player: Player,
    start: Location,
    dest: Location,
    startArrive: Int,
    destArrive: Int,
    dir: Direction? = null,
    anim: Int = 819,
    callback: (() -> Unit)? = null,
) {
    var direction: Direction

    if (dir == null) {
        var delta = Location.getDelta(start, dest)
        var x = abs(delta.x)
        var y = abs(delta.y)

        if (x > y) {
            direction = Direction.getDirection(delta.x, 0)
        } else {
            direction = Direction.getDirection(0, delta.y)
        }
    } else {
        direction = dir
    }

    val startLoc = Location.create(start)
    val destLoc = Location.create(dest)
    var startArriveTick = getWorldTicks() + cyclesToTicks(startArrive) + 1
    var destArriveTick = startArriveTick + cyclesToTicks(destArrive)
    var maskSet = false

    delayEntity(player, (destArriveTick - getWorldTicks()) + 1)
    queueScript(player, 0, QueueStrength.SOFT) {
        if (!finishedMoving(player)) return@queueScript keepRunning(player)
        if (!maskSet) {
            var ctx = ForceMoveCtx(startLoc, destLoc, startArrive, destArrive, direction)
            player.updateMasks.register(EntityFlag.ForceMove, ctx)
            maskSet = true
        }

        var tick = getWorldTicks()
        if (tick < startArriveTick) {
            return@queueScript keepRunning(player)
        } else if (tick < destArriveTick) {
            if (animationFinished(player)) animate(player, anim)
            return@queueScript keepRunning(player)
        } else if (tick >= destArriveTick) {
            try {
                callback?.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            player.properties.teleportLocation = dest
            return@queueScript stopExecuting(player)
        }
        return@queueScript stopExecuting(player)
    }
}

/**
 * Stops the walking action for the specified entity.
 *
 * @param entity The entity whose walking action will be stopped.
 */
fun stopWalk(entity: Entity) {
    entity.walkingQueue.reset()
}

/**
 * Retrieves the child object IDs associated with the specified scenery.
 *
 * @param scenery The scenery object ID.
 * @return An array of child object IDs for the given scenery.
 */
fun getChildren(scenery: Int): IntArray =
    SceneryDefinition
        .forId(scenery)
        .childrenIds!!
        .filter { it != -1 }
        .toIntArray()

/**
 * Adjusts the charge of a node (either an Item or a Scenery object) by a specified amount.
 *
 * @param node The node whose charge is to be adjusted (either an Item or Scenery).
 * @param amount The amount by which to adjust the charge.
 */
fun adjustCharge(
    node: Node,
    amount: Int,
) {
    when (node) {
        is Item -> node.charge += amount
        is Scenery -> node.charge += amount
        else ->
            log(
                ContentAPI::class.java,
                Log.ERR,
                "Attempt to adjust the charge of invalid type: ${node.javaClass.simpleName}",
            )
    }
}

/**
 * Retrieves the charge of a node (either an Item or a Scenery object).
 *
 * @param node The node whose charge is to be retrieved (either an Item or Scenery).
 * @return The charge of the node. Returns -1 if the node type is invalid.
 */
fun getCharge(node: Node): Int {
    when (node) {
        is Item -> return node.charge
        is Scenery -> return node.charge
        else ->
            log(
                ContentAPI::class.java,
                Log.ERR,
                "Attempt to get charge of invalid type: ${node.javaClass.simpleName}",
            ).also { return -1 }
    }
}

/**
 * Sets the charge of a node (either an Item or a Scenery object) to a specified value.
 *
 * @param node The node whose charge is to be set (either an Item or Scenery).
 * @param charge The charge value to set.
 */
fun setCharge(
    node: Node,
    charge: Int,
) {
    when (node) {
        is Item -> node.charge = charge
        is Scenery -> node.charge = charge
        else ->
            log(
                ContentAPI::class.java,
                Log.ERR,
                "Attempt to set the charge of invalid type: ${node.javaClass.simpleName}",
            )
    }
}

/**
 * Retrieves the option that the player used for interaction.
 *
 * @param player The player whose used option is to be retrieved.
 * @return The used interaction option.
 */
fun getUsedOption(player: Player): String = player.getAttribute("interact:option", "INVALID")

/**
 * Visualizes an animation and graphics effect for the specified entity.
 *
 * @param entity The entity to visualize the animation and graphics on.
 * @param anim The animation to visualize.
 * @param gfx The graphic effect to visualize.
 * @throws IllegalStateException If invalid parameters for animation or graphics are provided.
 */
fun <A, G> visualize(
    entity: Entity,
    anim: A,
    gfx: G,
) {
    val animation =
        when (anim) {
            is Int -> Animation(anim)
            is Animation -> anim
            else -> throw IllegalStateException("Invalid parameter passed for animation.")
        }

    val graphics =
        when (gfx) {
            is Int -> Graphics(gfx)
            is Graphics -> gfx
            else -> throw IllegalStateException("Invalid parameter passed for graphics.")
        }

    entity.visualize(animation, graphics)
}

/**
 * Submits a world pulse to the pulse manager for processing.
 *
 * @param pulse The pulse to submit.
 */
fun submitWorldPulse(pulse: Pulse) {
    Pulser.submit(pulse)
}

/**
 * Runs a task in the world pulse system.
 *
 * @param task The task to run.
 * @return The Pulse object associated with the task.
 */
fun runWorldTask(task: () -> Unit): Pulse {
    val pulse =
        object : Pulse() {
            override fun pulse(): Boolean {
                task.invoke()
                return true
            }
        }

    submitWorldPulse(pulse)
    return pulse
}

/**
 * Teleports the entity to a specified location.
 *
 * @param entity The entity to teleport.
 * @param loc The location to teleport to.
 * @param type The type of teleportation (default is instant).
 * @return True if teleportation was successful, false otherwise.
 */
fun teleport(
    entity: Entity,
    loc: Location,
    type: TeleportManager.TeleportType = TeleportManager.TeleportType.INSTANT,
): Boolean {
    if (type == TeleportManager.TeleportType.INSTANT) {
        entity.properties.teleportLocation = loc
        return true
    } else {
        return entity.teleporter.send(loc, type)
    }
}

/**
 * Teleports the entity to a specified location, optionally after a delay.
 *
 * @param entity The entity to teleport.
 * @param loc The location to teleport to.
 * @param type The type of teleportation (default is instant).
 * @param ticks Optional number of ticks to delay the teleport (default = null, instant).
 * @return True if teleportation was successful immediately, false if delayed.
 */
fun teleport(
    entity: Entity,
    loc: Location,
    type: TeleportManager.TeleportType = TeleportManager.TeleportType.INSTANT,
    ticks: Int? = null
): Boolean {
    return if (ticks == null || ticks <= 0) {
        entity.properties.teleportLocation = loc
        true
    } else {
        GameWorld.Pulser.submit(object : Pulse(ticks) {
            override fun pulse(): Boolean {
                if (type == TeleportManager.TeleportType.INSTANT) {
                    entity.properties.teleportLocation = loc
                } else {
                    entity.teleporter.send(loc, type)
                }
                return true
            }
        })
        false
    }
}

/**
 * Sets a temporary skill level for the entity.
 *
 * @param entity The entity whose skill level is to be set.
 * @param skill The skill to set the level for.
 * @param level The level to set the skill to.
 */
fun setTempLevel(
    entity: Entity,
    skill: Int,
    level: Int,
) {
    entity.skills.setLevel(skill, level)
}

/**
 * Retrieves the static (base) skill level for the entity.
 *
 * @param entity The entity whose skill level is to be retrieved.
 * @param skill The skill to retrieve the level for.
 * @return The static skill level of the entity.
 */
fun getStatLevel(
    entity: Entity,
    skill: Int,
): Int = entity.skills.getStaticLevel(skill)

/**
 * Retrieves the dynamic skill level for the entity, considering bonuses.
 *
 * @param entity The entity whose skill level is to be retrieved.
 * @param skill The skill to retrieve the level for.
 * @return The dynamic skill level of the entity.
 */
fun getDynLevel(
    entity: Entity,
    skill: Int,
): Int = entity.skills.getLevel(skill)

/**
 * Adjusts a skill level for the entity by a specified amount.
 *
 * @param entity The entity whose skill level is to be adjusted.
 * @param skill The skill to adjust.
 * @param amount The amount to adjust the skill level by.
 */
fun adjustLevel(
    entity: Entity,
    skill: Int,
    amount: Int,
) {
    entity.skills.setLevel(skill, entity.skills.getStaticLevel(skill) + amount)
}

/**
 * Drains a skill level of an entity by a percentage.
 *
 * @param entity The entity whose skill to drain.
 * @param skill The skill id.
 * @param drainPercentage The percentage to drain from the current dynamic level (0.0 - 1.0).
 * @param maximumDrainPercentage The maximum allowable drain from the static level (0.0 - 1.0).
 */
fun drainStatLevel(
    entity: Entity,
    skill: Int,
    drainPercentage: Double,
    maximumDrainPercentage: Double
) {
    entity.skills.drainLevel(skill, drainPercentage, maximumDrainPercentage)
}

/**
 * Removes all instances of a specified item from the player's specified container (Inventory, Equipment, Bank).
 *
 * @param player The player from whose container the item will be removed.
 * @param item The item or item ID to remove.
 * @param container The container to remove the item from (default is Inventory).
 * @return True if the item was successfully removed, false otherwise.
 */
fun <T> removeAll(player: Player, item: T, container: Container = Container.INVENTORY): Boolean {
    item ?: return false
    val it = when (item) {
        is Item -> item.id
        is Int -> item
        else -> throw IllegalStateException("Invalid value passed as item")
    }

    return when (container) {
        Container.EQUIPMENT -> player.equipment.remove(Item(it, amountInEquipment(player, it)))
        Container.BANK -> {
            val amountInPrimary = amountInBank(player, it)
            player.bank.remove(Item(it, amountInPrimary))
        }
        Container.INVENTORY -> player.inventory.remove(Item(it, amountInInventory(player, it)))
        Container.BoB -> {
            if (player.familiarManager.hasFamiliar() && player.familiarManager.familiar.isBurdenBeast){
                val cont = (player.familiarManager.familiar as BurdenBeast).container
                cont.remove(Item(it, cont.getAmount(it)))
            }
            else
                false
        }
    }
}

/**
 * Sends a string to the player to display in a specified interface and child component.
 *
 * @param player The player to send the string to.
 * @param string The string to display.
 * @param iface The interface ID to display the string in.
 * @param child The child component ID to display the string in.
 */
fun sendString(
    player: Player,
    string: String,
    iface: Int,
    child: Int,
) {
    player.packetDispatch.sendString(string, iface, child)
}

/**
 * Closes all interfaces currently open for the player.
 *
 * @param player The player whose interfaces will be closed.
 */
fun closeInterface(player: Player) {
    player.interfaceManager.close()
}

/**
 * Closes a single tab interface for the player.
 *
 * @param player The player whose tab interface will be closed.
 */
fun closeTabInterface(player: Player) {
    player.interfaceManager.closeSingleTab()
}

/**
 * Closes the chatbox interface for the player.
 *
 * @param player The player whose chatbox will be closed.
 */
fun closeChatBox(player: Player) {
    player.interfaceManager.closeChatbox()
}

/**
 * Closes the single tab for the player.
 *
 * @param player The player whose tab will be closed.
 */
fun closeSingleTab(player: Player) {
    player.interfaceManager.closeSingleTab()
}

/**
 * Sets the visibility of a component in the player's interface.
 *
 * @param player The player whose interface component visibility will be set.
 * @param iface The interface ID of the component.
 * @param child The child component ID.
 * @param hide Whether to hide or show the component.
 */
fun setComponentVisibility(
    player: Player,
    iface: Int,
    child: Int,
    hide: Boolean,
) {
    player.packetDispatch.sendInterfaceConfig(iface, child, hide)
}

/**
 * Sets the title component visibility for the player based on the provided options.
 *
 * @param player The player whose UI component will be modified.
 * @param options The options determining the visibility of components (should be between 2 and 5).
 * @throws IllegalArgumentException If options are outside the valid range.
 */
fun setTitle(player: Player, options: Int) {
    require(options in 2..5) { "Expected option value between 2 and 5, got $options." }

    val componentId = when (options) {
        5 -> Components.MULTI5_234
        4 -> Components.MULTI4_232
        3 -> Components.MULTI3_230
        else -> Components.MULTI2_228
    }

    setComponentVisibility(player, componentId, 4 + options, true)
    setComponentVisibility(player, componentId, if (options == 2 || options == 4) 9 else 10, false)
}

/**
 * Sends a dialogue message to the player.
 *
 * @param player The player who will receive the dialogue.
 * @param msg The message to display to the player.
 * @param expr The facial expression of the player while the dialogue is being displayed. Defaults to [FaceAnim.FRIENDLY].
 */
fun sendPlayerDialogue(
    player: Player,
    msg: String,
    expr: FaceAnim = FaceAnim.FRIENDLY,
) {
    player.dialogueInterpreter.sendDialogues(player, expr, *splitLines(msg))
}

/**
 * Sends the player model on a interface.
 *
 * @param player The player receiving the interface.
 * @param iface The interface ID to show the player.
 * @param child The child component ID of the interface.
 */
fun sendPlayerOnInterface(
    player: Player,
    iface: Int,
    child: Int,
) {
    player.packetDispatch.sendPlayerOnInterface(iface, child)
}

/**
 * Sends the npc model on a interface.
 *
 * @param player The player receiving the interface.
 * @param npcId The npc id.
 * @param iface The interface ID to show the player.
 * @param child The child component ID of the interface.
 */
fun sendNpcOnInterface(
    player: Player,
    npcId: Int,
    iface: Int,
    child: Int,
) {
    player.packetDispatch.sendNpcOnInterface(npcId, iface, child)
}


/**
 * Sends a dialogue message from an NPC to the player.
 *
 * @param player The player receiving the dialogue.
 * @param npc The NPC ID sending the message.
 * @param msg The message to display.
 * @param expr The facial expression of the NPC. Defaults to [FaceAnim.FRIENDLY].
 */
fun sendNPCDialogue(
    player: Player,
    npc: Int,
    msg: String,
    expr: FaceAnim = FaceAnim.FRIENDLY,
) {
    player.dialogueInterpreter.sendDialogues(npc, expr, *splitLines(msg))
}

/**
 * Sends a delayed dialogue message from an NPC to the player.
 *
 * @param player The player receiving the dialogue.
 * @param tick The delay (in game ticks) before the dialogue is shown.
 * @param npc The NPC ID sending the message.
 * @param msg The message to display.
 * @param expr The facial expression of the NPC. Defaults to [FaceAnim.FRIENDLY].
 */
fun sendNPCDialogueWithDelay(
    player: Player,
    tick: Int,
    npc: Int,
    msg: String,
    expr: FaceAnim = FaceAnim.FRIENDLY,
) {
    player.dialogueInterpreter.sendDialogues(tick, npc, expr, *splitLines(msg))
}

/**
 * Sends multiple lines of dialogue from an NPC to the player.
 *
 * @param player The player receiving the dialogue.
 * @param npc The NPC ID sending the message.
 * @param expr The facial expression of the NPC.
 * @param hideContinue If true, the "Continue" button will be hidden.
 * @param msgs The dialogue lines to send.
 */
fun sendNPCDialogueLines(
    player: Player,
    npc: Int,
    expr: FaceAnim,
    hideContinue: Boolean,
    vararg msgs: String,
) {
    val dialogueComponent = player.dialogueInterpreter.sendDialogues(npc, expr, *msgs)
    player.packetDispatch.sendInterfaceConfig(dialogueComponent.id, msgs.size + 4, hideContinue)
}

/**
 * Sends an animation on a specific interface.
 *
 * @param player The player receiving the animation.
 * @param anim The animation ID to play.
 * @param iface The interface ID where the animation will be shown.
 * @param child The child component ID within the interface.
 */
fun sendAnimationOnInterface(
    player: Player,
    anim: Int,
    iface: Int,
    child: Int,
) {
    player.packetDispatch.sendAnimationInterface(anim, iface, child)
}

/**
 * Registers a logout listener for a specific player.
 *
 * @param player The player to register the listener for.
 * @param key The unique key identifying the listener.
 * @param handler The function to execute when the player logs out.
 */
fun registerLogoutListener(
    player: Player,
    key: String,
    handler: (p: Player) -> Unit,
) {
    player.logoutListeners[key] = handler
}

/**
 * Clears a logout listener for a specific player.
 *
 * @param player The player whose listener will be removed.
 * @param key The unique key identifying the listener to remove.
 */
fun clearLogoutListener(
    player: Player,
    key: String,
) {
    player.logoutListeners.remove(key)
}

/**
 * Clears the player's inventory.
 *
 * @param player The player whose inventory will be cleared.
 */
fun clearInventory(player: Player) {
    player.inventory.clear()
}

/**
 * Clears the player's equipment.
 *
 * @param player The player whose equipment will be cleared.
 */
fun clearEquipment(player: Player) {
    player.equipment.clear()
}

/**
 * Clears both the player's inventory and equipment.
 *
 * @param player The player whose inventory and equipment will be cleared.
 */
fun clearInventoryAndEquipment(player: Player) {
    player.equipment.clear()
    player.inventory.clear()
}

/**
 * Sends an item to the player on a specific interface.
 *
 * @param player The player receiving the item.
 * @param iface The interface ID to show the player.
 * @param child The child component ID of the interface.
 * @param item The item to send.
 * @param amount The amount of the item to send. Defaults to 1.
 */
fun sendItemOnInterface(
    player: Player,
    iface: Int,
    child: Int,
    item: Int,
    amount: Int = 1,
) {
    player.packetDispatch.sendItemOnInterface(item, amount, iface, child)
}

/**
 * Sends a model to the player on a specific interface.
 *
 * @param player The player receiving the model.
 * @param iface The interface ID to show the player.
 * @param child The child component ID of the interface.
 * @param model The model ID to send.
 * @param zoom The zoom level of the model. Defaults to 1.
 */
fun sendModelOnInterface(
    player: Player,
    iface: Int,
    child: Int,
    model: Int,
    zoom: Int = 1,
) {
    player.packetDispatch.sendModelOnInterface(model, iface, child, zoom)
}

/**
 * Sends an angle (pitch, yaw, zoom) to the player on a specific interface.
 *
 * @param player The player receiving the angle data.
 * @param iface The interface ID to show the player.
 * @param child The child component ID of the interface.
 * @param zoom The zoom level.
 * @param pitch The pitch (up/down) angle.
 * @param yaw The yaw (left/right) angle.
 */
fun sendAngleOnInterface(
    player: Player,
    iface: Int,
    child: Int,
    zoom: Int,
    pitch: Int,
    yaw: Int,
) {
    player.packetDispatch.sendAngleOnInterface(iface, child, zoom, pitch, yaw)
}

/**
 * Sends an item dialogue to the player.
 *
 * @param player The player receiving the dialogue.
 * @param item The item involved in the dialogue.
 * @param message The message to display alongside the item.
 */
fun sendItemDialogue(
    player: Player,
    item: Any,
    message: String,
) {
    val dialogueItem =
        when (item) {
            is Item -> item
            is Int -> Item(item)
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Expected an Item or an Int, got ${item::class.java.simpleName}.",
                )
            }
        }
    player.dialogueInterpreter.sendItemMessage(dialogueItem, *splitLines(message))
}

/**
 * Sends a dialogue involving two items to the player.
 *
 * @param player The player receiving the dialogue.
 * @param primaryItem The first item involved in the dialogue.
 * @param secondaryItem The second item involved in the dialogue.
 * @param message The message to display alongside the items.
 */
fun sendDoubleItemDialogue(
    player: Player,
    primaryItem: Any,
    secondaryItem: Any,
    message: String
) {
    when {
        primaryItem is Item && secondaryItem is Item -> player.dialogueInterpreter.sendDoubleItemMessage(primaryItem, secondaryItem, message)
        primaryItem is Int && secondaryItem is Int -> player.dialogueInterpreter.sendDoubleItemMessage(primaryItem, secondaryItem, message)
        else -> throw IllegalArgumentException("Item types must match: both Item or both Int")
    }
}

/**
 * Sends an input dialogue to the player, allowing numeric or string input.
 *
 * @param player The player receiving the input dialogue.
 * @param numeric If true, the input will be numeric; otherwise, it will accept a string.
 * @param prompt The prompt message to show to the player.
 * @param handler The function to handle the player's input value.
 */
fun sendInputDialogue(
    player: Player,
    numeric: Boolean,
    prompt: String,
    handler: (value: Any) -> Unit,
) {
    if (numeric) {
        sendInputDialogue(player, InputType.NUMERIC, prompt, handler)
    } else {
        sendInputDialogue(player, InputType.STRING_SHORT, prompt, handler)
    }
}

/**
 * Sends an input dialogue to the player, with a specific input type.
 *
 * @param player The player receiving the input dialogue.
 * @param type The type of input (e.g., numeric, string, etc.).
 * @param prompt The prompt message to show to the player.
 * @param handler The function to handle the player's input value.
 */
fun sendInputDialogue(
    player: Player,
    type: InputType,
    prompt: String,
    handler: (value: Any) -> Unit,
) {
    when (type) {
        InputType.AMOUNT -> {
            setAttribute(player, "parseamount", true)
            player.dialogueInterpreter.sendInput(true, prompt)
        }

        InputType.NUMERIC, InputType.STRING_SHORT ->
            player.dialogueInterpreter.sendInput(
                type != InputType.NUMERIC,
                prompt,
            )

        InputType.STRING_LONG -> player.dialogueInterpreter.sendLongInput(prompt)
        InputType.MESSAGE -> player.dialogueInterpreter.sendMessageInput(prompt)
    }

    setAttribute(player, "runscript", handler)
    setAttribute(player, "input-type", type)
}

/**
 * Makes the entity flee from another entity.
 *
 * @param entity The entity that is fleeing.
 * @param from The entity that the first entity is fleeing from.
 */
fun flee(
    entity: Entity,
    from: Entity,
) {
    lock(entity, 5)
    face(entity, from, 5)

    val diffX = entity.location.x - from.location.x
    val diffY = entity.location.y - from.location.y

    forceWalk(entity, entity.location.transform(diffX, diffY, 0), "DUMB")
}

/**
 * Submits a pulse for an entity.
 *
 * @param entity The entity to execute the pulse for.
 * @param pulse The pulse to execute.
 */
fun submitIndividualPulse(
    entity: Entity,
    pulse: Pulse,
) {
    entity.pulseManager.run(pulse)
}

/**
 * Submits a pulse for an entity.
 *
 * @param entity The entity to execute the pulse for.
 * @param pulse The pulse to execute.
 * @param type The pulse type.
 */
fun submitIndividualPulse(
    entity: Entity,
    pulse: Pulse,
    type: PulseType
) {
    entity.pulseManager.run(pulse, type)
}

/**
 * Runs a task for an entity with a delay and repeat count.
 *
 * @param entity The entity to run the task for.
 * @param delay The delay before the task is run (in game ticks).
 * @param repeatTimes The number of times to repeat the task.
 * @param task The task to execute.
 */
fun runTask(
    entity: Entity,
    delay: Int = 0,
    repeatTimes: Int = 1,
    task: () -> Unit,
) {
    var cycles = repeatTimes
    entity.pulseManager.run(
        object : Pulse(delay) {
            override fun pulse(): Boolean {
                task.invoke()
                return --cycles <= 0
            }
        },
    )
}

/**
 * Sends an item with a zoom level to the player on a specific interface.
 *
 * @param player The player receiving the item.
 * @param iface The interface ID to show the player.
 * @param child The child component ID of the interface.
 * @param item The item ID to send.
 * @param zoom The zoom level of the item. Defaults to 230.
 */
fun sendItemZoomOnInterface(
    player: Player,
    iface: Int,
    child: Int,
    item: Int,
    zoom: Int = 230,
) {
    player.packetDispatch.sendItemZoomOnInterface(item, zoom, iface, child)
}

/**
 * Retrieves the scenery definition for a given ID.
 *
 * @param id The ID of the scenery.
 * @return The [SceneryDefinition] object corresponding to the ID.
 */
fun sceneryDefinition(id: Int): SceneryDefinition = SceneryDefinition.forId(id)

/**
 * Registers a map zone with specified borders.
 *
 * @param zone The map zone to register.
 * @param borders The borders that define the zone.
 */
fun registerMapZone(
    zone: MapZone,
    borders: ZoneBorders,
) {
    ZoneBuilder.configure(zone)
    zone.register(borders)
}

/**
 * Animates a specific interface with a given animation.
 *
 * @param player The player to animate.
 * @param iface The interface ID to animate.
 * @param child The child component ID to animate.
 * @param anim The animation ID to play.
 */
fun animateInterface(
    player: Player,
    iface: Int,
    child: Int,
    anim: Int,
) {
    player.packetDispatch.sendAnimationInterface(anim, iface, child)
}

/**
 * Adds a climb destination for a ladder.
 *
 * @param ladderLoc The location of the ladder.
 * @param dest The destination location after climbing.
 */
fun addClimbDest(
    ladderLoc: Location,
    dest: Location,
) {
    core.game.global.action.SpecialLadder
        .add(ladderLoc, dest)
}

/**
 * Sends a news message globally.
 *
 * @param message The message to send.
 */
fun sendNews(message: String) {
    Repository.sendNews(message, 12, "CC6600")
}

/**
 * Sends graphics to a specified location.
 *
 * @param gfx The graphics to send (either an integer ID or a [Graphics] object).
 * @param location The location where the graphics should be displayed.
 */
fun <G> sendGraphics(
    gfx: G,
    location: Location,
) {
    when (gfx) {
        is Int ->
            Graphics.send(
                Graphics(
                    gfx,
                ),
                location,
            )

        is Graphics -> Graphics.send(gfx, location)
    }
}

/**
 * Sends a graphic to the player.
 *
 * @param player    The player to send the graphic to.
 * @param graphics  The graphic, either an Int (ID) or a Graphics object.
 * @param height    The height to use if gfx is an Int (optional, default 0).
 */
fun sendGraphics(player: Player, graphics: Any, height: Int = 0) {
    when (graphics) {
        is Int -> {
            player.packetDispatch.sendGraphic(graphics, height)
        }
        is Graphics -> {
            player.packetDispatch.sendGraphic(graphics.id, graphics.height)
        }
        else -> throw IllegalArgumentException("graphics must be either an Int or a Graphics object")
    }
}

/**
 * Executes a run script on the player's client with a given script ID and arguments.
 *
 * @param player The player executing the script.
 * @param scriptId The ID of the script to run.
 * @param arguments The arguments to pass to the script.
 */
fun runcs2(
    player: Player,
    scriptId: Int,
    vararg arguments: Any,
) {
    var typeString = StringBuilder()
    var finalArgs = Array<Any?>(arguments.size) { null }
    try {
        for (i in 0 until arguments.size) {
            val arg = arguments[i]
            if (arg is Int) {
                typeString.append("i")
            } else if (arg is String) {
                typeString.append("s")
            } else {
                throw IllegalArgumentException(
                    "Argument at index $i ($arg) is not an acceptable type! Only string and int are accepted.",
                )
            }
            finalArgs[arguments.size - i - 1] = arg
        }
        player.packetDispatch.sendRunScript(scriptId, typeString.toString(), *finalArgs)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Sends an item selection interface to the player with the given options.
 *
 * @param player The player receiving the interface.
 * @param options The options to present to the player.
 * @param keepAlive If true, the interface will stay open after the selection.
 * @param callback The callback function to invoke when an option is selected.
 */
@JvmOverloads
fun sendItemSelect(
    player: Player,
    vararg options: String,
    keepAlive: Boolean = false,
    callback: (slot: Int, optionIndex: Int) -> Unit,
) {
    player.interfaceManager.openSingleTab(Component(Components.ITEM_SELECT_12))
    val scriptArgs = arrayOf((12 shl 16) + 18, 93, 4, 7, 0, -1, "", "", "", "", "", "", "", "", "")
    for (i in 0 until min(9, options.size)) scriptArgs[6 + i] = options[i]
    runcs2(player, 150, *scriptArgs)
    val settings = IfaceSettingsBuilder().enableOptions(0 until 9).build()
    player.packetDispatch.sendIfaceSettings(settings, 18, Components.ITEM_SELECT_12, 0, 28)
    setAttribute(player, "itemselect-callback", callback)
    setAttribute(player, "itemselect-keepalive", keepAlive)
}

/**
 * Announces a rare item drop for the player, if the item is rare.
 *
 * @param player The player receiving the rare item.
 * @param item The item being dropped.
 */
fun announceIfRare(
    player: Player,
    item: Item,
) {
    if (item.definition.getConfiguration(ItemConfigParser.RARE_ITEM, false)) {
        sendNews("${player.username} has just received: ${item.amount} x ${item.name}.")
        PlayerStatsCounter.incrementRareDrop(player, item)
    }
}

/**
 * Checks if the player has met the requirements for a given quest.
 *
 * @param player The player checking the requirements.
 * @param req The quest requirements to check.
 * @param message If true, messages will be sent to the player about unmet requirements.
 * @return True if the player meets the requirements, false otherwise.
 */
fun hasRequirement(
    player: Player,
    req: QuestReq,
    message: Boolean = true,
): Boolean {
    var (isMet, unmetReqs) = req.evaluate(player)
    val messageList = ArrayList<String>()

    var totalSoftQp = 0
    var totalHardQp = 0

    for (req in unmetReqs) {
        when (req) {
            is QPCumulative -> totalSoftQp += req.amount
            is QPReq -> if (req.amount > totalHardQp) totalHardQp = req.amount
        }
    }

    var neededQp = min(max(totalSoftQp, totalHardQp), player.questRepository.availablePoints)

    isMet = isMet && neededQp <= player.questRepository.points

    if (isMet) return true

    if (unmetReqs.size == 2 && unmetReqs[0] is QuestReq) {
        messageList.add("You must complete ${(unmetReqs[0] as QuestReq).questReq.questName} to use this.")
    } else {
        messageList.add("You need the pre-reqs for ${req.questReq.questName} to access this.")
        messageList.add("Please check the quest journal for more information.")
    }

    if (message) for (message in messageList) sendMessage(player, message)

    return false
}

/**
 * Retrieves the names of skills the player has mastered (level 99).
 *
 * @param player The player whose mastered skills are to be retrieved.
 * @return A list of skill names the player has mastered.
 */
fun getMasteredSkillNames(player: Player): List<String> {
    val hasMastered = player.getSkills().masteredSkills > 0
    val masteredSkills = ArrayList<String>()

    if (hasMastered) {
        for ((skillId, skillName) in Skills.SKILL_NAME.withIndex()) {
            if (hasLevelStat(player, skillId, 99)) {
                masteredSkills.add(skillName)
            }
        }
    }
    return masteredSkills
}

/**
 * Dumps the contents of a container into the player's bank.
 *
 * @param player The player performing the action.
 * @param container The container whose items are being deposited.
 * @return The number of items successfully dumped into the bank.
 */
fun dumpContainer(
    player: Player,
    container: core.game.container.Container,
): Int {
    val bank = player.bank
    var dumpedCount = 0

    run beginDepositing@{
        container.toArray().filterNotNull().forEach { item ->
            if (!bank.hasSpaceFor(item)) {
                val message =
                    if (item.id ==
                        Items.COINS_995
                    ) {
                        "You have ran out of Bank Space. Please make more room."
                    } else {
                        "You have no more space in your bank."
                    }
                sendMessage(player, message)
                return@beginDepositing
            }

            if (!bank.canAdd(item)) {
                sendMessage(player, "A magical force prevents you from banking the ${item.name}.")
                return@forEach
            } else {
                if (container is EquipmentContainer) {
                    if (!InteractionListeners.run(item.id, player, item, false)) {
                        sendMessage(player, "A magical force prevents you from removing your ${item.name}.")
                        return@forEach
                    }
                }

                container.remove(item)
                bank.add(unnote(item), true)
                dumpedCount++
            }
        }
    }

    player.bank.refresh()
    return dumpedCount
}

/**
 * Dumps the contents of the player's Beast of Burden familiar into the bank.
 *
 * @param player The player performing the action.
 */
fun dumpBeastOfBurden(player: Player) {
    val famMan = player.familiarManager

    if (!famMan.hasFamiliar()) {
        sendMessage(player, "You don't have a familiar.")
        return
    }

    if (famMan.familiar !is content.global.skill.summoning.familiar.BurdenBeast) {
        sendMessage(player, "Your familiar is not a Beast of Burden.")
        return
    }

    val beast: content.global.skill.summoning.familiar.BurdenBeast =
        (famMan.familiar as content.global.skill.summoning.familiar.BurdenBeast)

    if (beast.container.isEmpty) {
        sendMessage(player, "Your familiar's inventory is empty.")
        return
    }

    val itemCount = beast.container.itemCount()
    val dumpedCount = dumpContainer(player, beast.container)

    when {
        dumpedCount == itemCount -> sendMessage(player, "Your familiar's inventory was deposited into your bank.")
        dumpedCount > 0 -> {
            val remainPhrase =
                when {
                    (itemCount - dumpedCount == 1) -> "item remains"
                    else -> "items remain"
                }

            sendMessage(player, "${itemCount - dumpedCount} $remainPhrase in your familiar's inventory.")
        }
    }
}

/**
 * Retrieves the skill boost provided by the player's familiar for a given skill.
 *
 * @param player The player whose familiar is being checked.
 * @param skill The skill ID to check the boost for.
 * @return The boost provided by the familiar.
 */
fun getFamiliarBoost(
    player: Player,
    skill: Int,
): Int = player.familiarManager.getBoost(skill)

/**
 * Converts an item to its noted form, if possible.
 *
 * @param item The item to convert.
 * @return The noted version of the item.
 */
fun note(item: Item): Item {
    if (!item.definition.isUnnoted()) return item

    if (item.definition.noteId < 0) return item

    return Item(item.definition.noteId, item.amount, item.charge)
}

/**
 * Converts an item from its noted form to its unnoted version.
 *
 * @param item The item to unnote.
 * @return The unnoted version of the item.
 */
fun unnote(item: Item): Item {
    if (item.definition.isUnnoted()) return item

    return Item(item.noteChange, item.amount, item.charge)
}

/**
 * Checks if the player has the Seal of Passage in their inventory or equipment.
 *
 * @param player The player to check.
 * @return True if the player has the Seal of Passage, false otherwise.
 */
fun hasSealOfPassage(player: Player): Boolean = inEquipmentOrInventory(player, Items.SEAL_OF_PASSAGE_9083)

/**
 * Checks if the player has a house.
 *
 * @param player The player to check.
 * @return True if the player has a house, false otherwise.
 */
fun hasHouse(player: Player): Boolean = player.houseManager.hasHouse()

/**
 * Retrieves the cutscene currently assigned to the player.
 *
 * @param player The player to retrieve the cutscene for.
 * @return The [Cutscene] object assigned to the player, or null if none exists.
 */
fun Player.getCutscene(): Cutscene? = getAttribute<Cutscene?>(this, Cutscene.ATTRIBUTE_CUTSCENE, null)

/**
 * Retrieves the current cutscene stage for the player.
 *
 * @return The cutscene stage.
 */
fun Player.getCutsceneStage(): Int = getAttribute(this, Cutscene.ATTRIBUTE_CUTSCENE_STAGE, 0)

/**
 * Retrieves the server configuration as a Toml object.
 *
 * @return The server configuration.
 */
fun getServerConfig(): Toml = ServerConfigParser.tomlData ?: Toml()

/**
 * Attempts to find a pathable random location within a specified radius around a given center location.
 *
 * @param target The target entity.
 * @param radius The radius within which to search for a pathable location.
 * @param center The center location from where the search will be initiated.
 * @param maxAttempts The maximum number of attempts to find a valid location (default is 3).
 * @return A pathable location if found, otherwise the target's current location.
 */
fun getPathableRandomLocalCoordinate(
    target: Entity,
    radius: Int,
    center: Location,
    maxAttempts: Int = 3,
): Location {
    var maxRadius = Vector.deriveWithEqualComponents(ServerConstants.MAX_PATHFIND_DISTANCE.toDouble()).x - 1
    var effectiveRadius = min(radius, maxRadius.toInt())
    val swCorner = center.transform(-effectiveRadius, -effectiveRadius, center.z)
    val neCorner = center.transform(effectiveRadius, effectiveRadius, center.z)
    val borders = ZoneBorders(swCorner.x, swCorner.y, neCorner.x, neCorner.y, center.z)

    var attempts = maxAttempts
    var success: Boolean
    while (attempts-- > 0) {
        val dest = borders.randomLoc
        val path = Pathfinder.find(center, dest, target.size())
        success = path.isSuccessful && !path.isMoveNear
        if (success) return dest
    }

    return target.location
}

/**
 * Attempts to find a pathable location in a cardinal direction from the given center location.
 *
 * @param target The target entity.
 * @param center The center location to check from.
 * @return A pathable location if found, otherwise the original center location.
 */
fun getPathableCardinal(
    target: Entity,
    center: Location,
): Location {
    var tiles = center.getCardinalTiles()

    for (tile in tiles) {
        val path = Pathfinder.find(center, tile, target.size())
        if (path.isSuccessful && !path.isMoveNear) return tile
    }

    return center
}

/**
 * Registers a hint icon for a player at the specified location.
 *
 * @param player The player to register the hint icon for.
 * @param location The location to place the hint icon.
 * @param height The height of the hint icon.
 */
fun registerHintIcon(
    player: Player,
    location: Location,
    height: Int,
) {
    setAttribute(
        player,
        "hinticon",
        HintIconManager.registerHintIcon(player, location, 1, -1, player.hintIconManager.freeSlot(), height, 3),
    )
}

/**
 * Registers a hint icon for a player at the location of a specific node.
 *
 * @param player The player to register the hint icon for.
 * @param node The node to register the hint icon for.
 */
fun registerHintIcon(
    player: Player,
    node: Node,
) {
    if (getAttribute(player, "hinticon", null) != null) return
    setAttribute(player, "hinticon", HintIconManager.registerHintIcon(player, node))
}

/**
 * Clears the hint icon for a player.
 *
 * @param player The player whose hint icon will be cleared.
 */
fun clearHintIcon(player: Player) {
    val slot = getAttribute(player, "hinticon", -1)
    removeAttribute(player, "hinticon")
    HintIconManager.removeHintIcon(player, slot)
}

/**
 * Checks if the player has their hands free (i.e., not equipped with a shield, weapon, or item in the hands slot).
 *
 * @param player The player to check.
 * @return True if the player's hands are free, otherwise false.
 */
fun hasHandsFree(player: Player): Boolean {
    val equipment = player.equipment
    return equipment[EquipmentContainer.SLOT_HANDS] == null &&
        equipment[EquipmentContainer.SLOT_SHIELD] == null &&
        equipment[EquipmentContainer.SLOT_WEAPON] == null
}

/**
 * Retrieves the equipment slot for a specific item, if any.
 *
 * @param item The item ID.
 * @return The corresponding equipment slot, or null if no slot is found.
 */
fun equipSlot(item: Int): EquipmentSlot? =
    EquipmentSlot.values().getOrNull(itemDefinition(item).getConfiguration(ItemConfigParser.EQUIP_SLOT, -1))

/**
 * Checks if the provided node is a player.
 *
 * @param node The node to check.
 * @return True if the node is a player, otherwise false.
 */
fun isPlayer(node: Node): Boolean = (node is Player)

/**
 * Adds a dialogue action to a player's dialogue queue.
 *
 * @param player The player to add the dialogue action to.
 * @param action The dialogue action to add.
 */
fun addDialogueAction(
    player: Player,
    action: DialogueAction,
) {
    player.dialogueInterpreter.addAction(action)
}

/**
 * Logs a message with a specific origin and type.
 *
 * @param origin The class from which the log is originating.
 * @param type The type of log (e.g., error, warning).
 * @param message The log message.
 */
fun log(
    origin: Class<*>,
    type: Log,
    message: String,
) {
    SystemLogger.processLogEntry(origin, type, message)
}

/**
 * Logs a message along with the stack trace of the exception.
 *
 * @param origin The class from which the log is originating.
 * @param type The type of log (e.g., error, warning).
 * @param message The log message.
 */
fun logWithStack(
    origin: Class<*>,
    type: Log,
    message: String,
) {
    try {
        throw Exception(message)
    } catch (e: Exception) {
        log(origin, type, "${exceptionToString(e)}")
    }
}

/**
 * Converts an exception to a string format including the stack trace.
 *
 * @param e The exception to convert.
 * @return The string representation of the exception with the stack trace.
 */
fun exceptionToString(e: Exception): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    return sw.toString()
}

/**
 * Delays the script execution for an entity by a specified number of ticks.
 *
 * @param entity The entity whose script is to be delayed.
 * @param ticks The number of ticks to delay.
 * @return False after delaying the script.
 */
fun delayScript(
    entity: Entity,
    ticks: Int,
): Boolean {
    entity.scripts.getActiveScript()?.let { it.nextExecution = GameWorld.ticks + ticks }
    return false
}

/**
 * Delays the entity's script execution by a specified number of ticks.
 *
 * @param entity The entity whose script will be delayed.
 * @param ticks The number of ticks to delay.
 */
fun delayEntity(
    entity: Entity,
    ticks: Int,
) {
    entity.scripts.delay = GameWorld.ticks + ticks
    lock(entity, ticks)
}

/**
 * Sets the action points range for the entity.
 *
 * @param entity The entity to modify.
 * @param apRange The range of action points to set.
 */
fun apRange(
    entity: Entity,
    apRange: Int,
) {
    entity.scripts.apRange = apRange
    entity.scripts.apRangeCalled = true
}

/**
 * Checks if an entity has line of sight to a target node.
 *
 * @param entity The entity to check.
 * @param target The target node.
 * @return True if the entity has line of sight to the target, otherwise false.
 */
fun hasLineOfSight(
    entity: Entity,
    target: Node,
): Boolean = CombatSwingHandler.isProjectileClipped(entity, target, false)

/**
 * Checks if the animation for an entity has finished.
 *
 * @param entity The entity to check.
 * @return True if the animation has finished, otherwise false.
 */
fun animationFinished(entity: Entity): Boolean = entity.clocks[Clocks.ANIMATION_END] < GameWorld.ticks

/**
 * Clears all scripts associated with an entity.
 *
 * @param entity The entity to clear scripts for.
 * @return True after clearing the scripts.
 */
fun clearScripts(entity: Entity): Boolean {
    entity.scripts.reset()
    return true
}

/**
 * Restarts the active script of an entity if it is persistent.
 *
 * @param entity The entity whose script should be restarted.
 * @return True if the script was restarted, otherwise false.
 */
fun restartScript(entity: Entity): Boolean {
    if (entity.scripts.getActiveScript()?.persist != true) {
        log(
            entity.scripts.getActiveScript()!!::class.java,
            Log.ERR,
            "Tried to call restartScript on a non-persistent script! Either use stopExecuting() or make the script persistent.",
        )
        return clearScripts(entity)
    }
    return true
}

/**
 * Ensures that the entity's script continues to run by setting the next execution tick.
 *
 * @param entity The entity whose script should keep running.
 * @return False after ensuring the script keeps running.
 */
fun keepRunning(entity: Entity): Boolean {
    entity.scripts.getActiveScript()?.nextExecution = getWorldTicks() + 1
    return false
}

/**
 * Stops the execution of an entity's script.
 *
 * @param entity The entity whose script should be stopped.
 * @return True if the script was stopped, otherwise false.
 */
fun stopExecuting(entity: Entity): Boolean {
    if (entity.scripts.getActiveScript()?.persist == true) {
        log(
            entity.scripts.getActiveScript()!!::class.java,
            Log.ERR,
            "Tried to call stopExecuting() on a persistent script! To halt execution of a persistent script, you MUST call clearScripts()!",
        )
        return clearScripts(entity)
    }
    return true
}

/**
 * Queues a script for execution with optional delay, strength, and persistence.
 *
 * @param entity The entity to queue the script for.
 * @param delay The number of ticks to delay before execution.
 * @param strength The strength of the queue (e.g., weak or strong).
 * @param persist Whether the script should persist across executions.
 * @param script The script to execute, which takes an integer stage.
 */
fun queueScript(
    entity: Entity,
    delay: Int = 1,
    strength: QueueStrength = QueueStrength.WEAK,
    persist: Boolean = false,
    script: (stage: Int) -> Boolean,
) {
    val s = QueuedScript(script, strength, persist)
    s.nextExecution = getWorldTicks() + delay
    entity.scripts.addToQueue(s, strength)
}

/**
 * Stuns an entity for a specified number of ticks.
 *
 * @param entity The entity to stun.
 * @param ticks The number of ticks to stun the entity.
 */
fun stun(
    entity: Entity,
    ticks: Int,
) {
    stun(entity, ticks, true)
}

/**
 * Stuns an entity for a specified number of ticks with an optional message.
 *
 * @param entity The entity to stun.
 * @param ticks The number of ticks to stun the entity.
 * @param sendMessage Whether to send a stun message to the player.
 */
fun stun(
    entity: Entity,
    ticks: Int,
    sendMessage: Boolean,
) {
    entity.walkingQueue.reset()
    entity.pulseManager.clear()
    entity.locks.lockMovement(ticks)
    entity.clocks[Clocks.STUN] = getWorldTicks() + ticks
    entity.graphics(Graphics(80, 96))
    if (entity is Player) {
        playAudio(entity.asPlayer(), Sounds.STUNNED_2727)
        entity.animate(Animation(424, Animator.Priority.VERY_HIGH))
        if (sendMessage) {
            sendMessage(entity, "You've been stunned!")
        }
    }
}

/**
 * Sets the current script state for an entity.
 *
 * @param entity The entity whose script state should be set.
 * @param state The new script state.
 */
fun setCurrentScriptState(
    entity: Entity,
    state: Int,
) {
    val script = entity.scripts.getActiveScript()
    if (script == null) {
        log(ContentAPI::class.java, Log.WARN, "Tried to set a script state when no script was being ran!")
        if (GameWorld.settings?.isDevMode != true) return
        throw IllegalStateException("Script execution mistake - check stack trace and above warning log!")
    }
    script.state =
        state - 1
}

/**
 * Modifies the prayer points of a player.
 *
 * @param player The player to modify prayer points for.
 * @param amount The amount to modify prayer points by (can be positive or negative).
 */
fun modPrayerPoints(
    player: Player,
    amount: Double,
) {
    if (amount > 0) {
        player.skills.incrementPrayerPoints(amount)
    } else if (amount < 0) {
        player.skills.decrementPrayerPoints(amount.absoluteValue)
    } else {
        return
    }
}

/**
 * Checks if a player's achievement diary is complete for a specific type and level.
 *
 * @param player The player to check.
 * @param type The type of the diary (e.g., a specific region).
 * @param level The level of the diary to check.
 * @return True if the diary is complete, otherwise false.
 */
fun isDiaryComplete(
    player: Player,
    type: DiaryType,
    level: Int,
): Boolean = player.achievementDiaryManager.getDiary(type)!!.isComplete(level)

/**
 * Checks if a specific diary task is completed for a player.
 *
 * @param player The player to check.
 * @param type The type of the diary (e.g., a specific region).
 * @param level The level of the diary.
 * @param index The index of the task to check.
 * @return True if the task is completed, otherwise false.
 */
fun hasDiaryTaskComplete(
    player: Player,
    type: DiaryType,
    level: Int,
    index: Int,
): Boolean = player.achievementDiaryManager.hasCompletedTask(type, level, index)

/**
 * Checks if an entity is within a specified distance from another location.
 *
 * @param entity The entity to check.
 * @param other The location to check distance to.
 * @param distance The distance to check (optional, defaults to the entity's distance).
 * @return True if within the specified distance, otherwise false.
 */
fun withinDistance(
    entity: Entity,
    other: Location,
    distance: Int? = null,
): Boolean {
    if (distance != null) {
        entity.location.withinDistance(other, distance)
    } else {
        entity.location.withinDistance(other)
    }

    return true
}

/**
 * Completes a specific diary task for a player.
 *
 * @param player The player to complete the task for.
 * @param type The type of the diary.
 * @param level The level of the diary.
 * @param task The task to complete.
 */
fun finishDiaryTask(
    player: Player,
    type: DiaryType,
    level: Int,
    task: Int,
) {
    player.achievementDiaryManager.finishTask(player, type, level, task)
}

/**
 * Checks if a specific prayer is active for a player.
 *
 * @param player The player to check.
 * @param type The prayer type to check.
 * @return True if the prayer is active, otherwise false.
 */
fun isPrayerActive(
    player: Player,
    type: PrayerType,
): Boolean = player.prayer.active.contains(type)

/**
 * Removes one or more interface tabs from a player's interface.
 *
 * @param player The player to remove the tabs from.
 * @param tabs The tabs to remove.
 */
fun removeTabs(
    player: Player,
    vararg tabs: Int,
) = player.interfaceManager.removeTabs(*tabs)

/**
 * Resets a player's camera view.
 *
 * @param player The player whose camera should be reset.
 */
fun resetCamera(player: Player) {
    try {
        PlayerCamera(player).reset()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Refreshes a player's inventory to reflect the current state.
 *
 * @param player The player whose inventory will be refreshed.
 */
fun refreshInventory(player: Player) {
    player.inventory.refresh()
}

/**
 * Refreshes a player appearance to reflect the current state.
 *
 * @param player The player whose appearance will be refreshed.
 */
fun refreshAppearance(player : Player) {
    player.appearance.sync()
}

/**
 * Delays the next attack of an entity by a specified number of ticks.
 *
 * @param entity The entity whose attack is being delayed.
 * @param ticks The number of game ticks to delay the next attack.
 */
fun delayAttack(
    entity: Entity,
    ticks: Int,
) {
    entity.properties.combatPulse.delayNextAttack(3)
    entity.clocks[Clocks.NEXT_ATTACK] = getWorldTicks() + ticks
}

/**
 * Restores the player's interface tabs to their default state.
 *
 * @param player The player whose interface tabs are being restored.
 */
fun restoreTabs(player: Player) = player.interfaceManager.restoreTabs()

/**
 * Opens a specific interface tab for a player.
 *
 * @param player The player who will open the interface tab.
 * @param component The id of the component (interface tab) to open.
 */
fun openSingleTab(
    player: Player,
    component: Int,
) {
    player.interfaceManager.openSingleTab(Component(component))
}

/**
 * Sets the state of the player's minimap.
 *
 * ```
 * // States
 * 0 - visible
 * 1 - locked
 * 2 - hidden
 * ```
 *
 * @param player The player whose minimap state is being updated.
 * @param state The state of the minimap.
 */
fun setMinimapState(
    player: Player,
    state: Int,
) = PacketRepository.send(MinimapState::class.java, MinimapStateContext(player, state))

/**
 * Show (unhide) the minimap.
 */
fun showMinimap(player: Player) {
    PacketRepository.send(MinimapState::class.java, MinimapStateContext(player, 0))
}

/**
 * Hide the minimap.
 */
fun hideMinimap(player: Player) {
    PacketRepository.send(MinimapState::class.java, MinimapStateContext(player, 2))
}

/**
 * Sends an interface configuration update to the player.
 *
 * @param player The player to whom the interface configuration is being sent.
 * @param interfaceId The interface id containing the child component.
 * @param childId The id of the specific child component within the interface.
 * @param hide If true, the child component will be hidden; if false, it will be shown.
 */
fun sendInterfaceConfig(
    player: Player,
    interfaceId: Int,
    childId: Int,
    hide: Boolean,
) {
    player.packetDispatch.sendInterfaceConfig(interfaceId, childId, hide)
}

/**
 * Repositions a child interface element for the player.
 *
 * @param player The player whose interface is being modified.
 * @param interfaceId The interface id.
 * @param childId The id of the element to move.
 * @param positionX The new X coordinate.
 * @param positionY The new Y coordinate.
 */
fun repositionChild(
    player: Player,
    interfaceId: Int,
    childId: Int,
    positionX: Int,
    positionY: Int,
) = PacketRepository.send(
    RepositionChild::class.java,
    ChildPositionContext(player, interfaceId, childId, positionX, positionY),
)

/**
 * Lock emote for player.
 *
 * @param player the player.
 * @param emoteId the id of emote.
 * @throws IllegalArgumentException if emoteId is invalid.
 */
fun lockEmote(player: Player, emoteId: Any) {
    val emote = when (emoteId) {
        is Emotes -> emoteId
        is Int -> {
            val emotes = Emotes.values()
            if (emoteId !in emotes.indices) {
                throw IllegalArgumentException("Invalid emote ID: [$emoteId]!")
            }
            emotes[emoteId]
        }
        else -> throw IllegalArgumentException("Parameter must be [Emotes] or [Int]!")
    }
    player.emoteManager.lock(emote)
    player.emoteManager.refresh()
}

/**
 * Unlock emote for player.
 *
 * @param player the player.
 * @param emoteId the id of emote.
 * @throws IllegalArgumentException if emoteId is invalid.
 */
fun unlockEmote(player: Player, emoteId: Any) {
    val emote = when (emoteId) {
        is Emotes -> emoteId
        is Int -> {
            val emotes = Emotes.values()
            if (emoteId !in emotes.indices) {
                throw IllegalArgumentException("Invalid emote ID: [$emoteId]!")
            }
            emotes[emoteId]
        }
        else -> throw IllegalArgumentException("Parameter must use [Emotes] or [Int]!")
    }
    player.emoteManager.unlock(emote)
    player.emoteManager.refresh()
}

/**
 * Runs the given emote for the given entity.
 *
 * @param entity the entity to run the emote on.
 * @param emote the Emotes enum entry to run.
 */
fun emote(entity: Entity, emote: Emotes) {
    entity.animate(emote.animation?.let { Animation(it) })
}

/**
 * Check if emote is unlocked for player.
 *
 * @param player the player.
 * @param emoteId the id of emote.
 * @return true if emote is unlocked, false otherwise.
 * @throws IllegalArgumentException if emoteId is invalid.
 */
fun hasEmote(player: Player, emoteId: Any): Boolean {
    val emote = when (emoteId) {
        is Emotes -> emoteId
        is Int -> {
            val emotes = Emotes.values()
            if (emoteId !in emotes.indices) {
                throw IllegalArgumentException("Invalid emote ID: [$emoteId]!")
            }
            emotes[emoteId]
        }
        else -> throw IllegalArgumentException("Parameter must be [Emotes] or [Int]!")
    }

    return player.emoteManager.isUnlocked(emote)
}

/**
 * Closes the current dialogue and chatbox for the player.
 *
 * @param player The player whose dialogue and chatbox are being closed.
 */
fun closeDialogue(player: Player) {
    player.dialogueInterpreter.close()
    player.interfaceManager.closeChatbox()
}

/**
 * A builder class for constructing a skill dialogue, allowing customization of the items displayed,
 * as well as the logic for item creation and maximum amount calculations.
 */
class SkillDialogueBuilder {
    internal lateinit var player: Player
    internal var items: Array<Item> = arrayOf<Item>()
    internal var creationCallback: (itemId: Int, amount: Int) -> Unit = { _, _ -> }
    internal var totalAmountCallback: (itemId: Int) -> Int = { id -> amountInInventory(player, id) }

    /**
     * Sets the items to be included in the skill dialogue.
     *
     * @param item The items to be added to the dialogue.
     */
    fun withItems(vararg item: Item) {
        items = arrayOf(*item)
    }

    /**
     * Sets the items to be included in the skill dialogue by their item ids.
     *
     * @param item The item ids to be added to the dialogue.
     */
    fun withItems(vararg item: Int) {
        items = item.map { Item(it) }.toTypedArray()
    }

    /**
     * Sets the callback method that will be invoked when an item is created.
     *
     * @param method The method that takes an item id and the amount of that item.
     */
    fun create(method: (itemId: Int, amount: Int) -> Unit) {
        creationCallback = method
    }

    /**
     * Sets the callback method that calculates the total amount of an item available for the player.
     *
     * @param method The method that calculates the amount of an item.
     */
    fun calculateMaxAmount(method: (itemId: Int) -> Int) {
        totalAmountCallback = method
    }
}

/**
 * Sends a skill dialogue to the player, allowing the player to choose from a set of items for a skill interaction.
 *
 * The skill dialogue is constructed based on the provided builder, and validation is performed to ensure
 * the correct number of items is passed.
 *
 * @param player The player who will receive the skill dialogue.
 * @param init The lambda block to initialize the skill dialogue using the builder.
 * @throws IllegalStateException if the number of items in the dialogue is not between 1 and 5.
 */
fun sendSkillDialogue(
    player: Player,
    init: SkillDialogueBuilder.() -> Unit,
) {
    val builder = SkillDialogueBuilder()
    builder.player = player
    builder.init()

    if (builder.items.size !in 1..5) {
        throw IllegalStateException(
            "Invalid number of items passed to skill dialogue (min 1, max 5): ${builder.items.size}",
        )
    }

    val type =
        when (builder.items.size) {
            1 -> SkillDialogueHandler.SkillDialogue.ONE_OPTION
            2 -> SkillDialogueHandler.SkillDialogue.TWO_OPTION
            3 -> SkillDialogueHandler.SkillDialogue.THREE_OPTION
            4 -> SkillDialogueHandler.SkillDialogue.FOUR_OPTION
            5 -> SkillDialogueHandler.SkillDialogue.FIVE_OPTION
            else -> null
        }

    object : SkillDialogueHandler(player, type, *builder.items) {
        /**
         * Handles the creation of an item when selected in the dialogue.
         *
         * @param amount The amount of the selected item to be created.
         * @param index The index of the selected item.
         */
        override fun create(
            amount: Int,
            index: Int,
        ) {
            builder.creationCallback(builder.items[index].id, amount)
        }

        /**
         * Retrieves the total amount of the selected item available for the player.
         *
         * @param index The index of the selected item.
         * @return The total amount of the item available for the player.
         */
        override fun getAll(index: Int): Int = builder.totalAmountCallback(builder.items[index].id)
    }.open()
}

/**
 * Gets the appropriate skilling tool for the player.
 **
 * @param player The player for whom the tool is being retrieved.
 * @param pickaxe If true, the function returns a pickaxe; otherwise, it returns a hatchet.
 * @return The appropriate [SkillingTool] for the player, or null if none is available.
 */
fun getTool(
    player: Player,
    pickaxe: Boolean,
): SkillingTool? = if (pickaxe) SkillingTool.getPickaxe(player) else SkillingTool.getAxe(player)

/**
 * Delays an entity's clock by a specified number of ticks.
 *
 * @param entity The entity whose clock is being delayed.
 * @param clock The clock ID to update (defines which action or event is delayed).
 * @param ticks The number of game ticks to delay the clock by.
 * @return Always returns false.
 */
fun delayClock(
    entity: Entity,
    clock: Int,
    ticks: Int,
): Boolean {
    entity.clocks[clock] = getWorldTicks() + ticks
    return false
}

/**
 * Checks if the specified clock for an entity has expired and is ready for use.
 *
 * @param entity The entity whose clock is being checked.
 * @param clock The clock ID to check.
 * @return True if the clock is ready, otherwise false.
 */
fun clockReady(
    entity: Entity,
    clock: Int,
): Boolean = entity.clocks[clock] <= getWorldTicks()

/**
 * Gets the total number of quest points a player has earned.
 *
 * @param player The player whose quest points are being retrieved.
 * @return The total quest points the player has earned.
 */
fun getQuestPoints(player: Player): Int = player.questRepository.points

/**
 * Retrieves the current stage of a specific quest for a player.
 *
 * @param player The player whose quest stage is being retrieved.
 * @param quest The name of the quest.
 * @return The stage of the quest, or 0 if the quest is not started.
 */
fun getQuestStage(
    player: Player,
    quest: String,
): Int = player.questRepository.getStage(quest)

/**
 * Sets the stage of a specific quest for a player.
 *
 * @param player The player whose quest stage is being set.
 * @param quest The name of the quest.
 * @param stage The new stage to set for the quest.
 */
fun setQuestStage(
    player: Player,
    quest: String,
    stage: Int,
) {
    player.questRepository.setStage(QuestRepository.getQuests()[quest]!!, stage)
    player.questRepository.syncronizeTab(player)
}

/**
 * Updates the quest tab to reflect the latest quest information for a player.
 *
 * @param player The player whose quest tab is being updated.
 */
fun updateQuestTab(player: Player) {
    player.questRepository.syncronizeTab(player)
}

/**
 * Checks if a quest is in progress for a player within a specified stage range.
 *
 * @param player The player whose quest status is being checked.
 * @param quest The name of the quest.
 * @param startStage The starting stage of the quest to check.
 * @param endStage The ending stage of the quest to check.
 * @return True if the player's quest is within the specified range, otherwise false.
 */
fun isQuestInProgress(
    player: Player,
    quest: String,
    startStage: Int,
    endStage: Int,
): Boolean = player.questRepository.getStage(quest) in startStage..endStage

/**
 * Checks if a quest is complete for a player (stage 100).
 *
 * @param player The player whose quest completion status is being checked.
 * @param quest The name of the quest.
 * @return True if the quest is complete, otherwise false.
 */
fun isQuestComplete(
    player: Player,
    quest: String,
): Boolean = player.questRepository.getStage(quest) == 100

/**
 * Gets the quest object for a player.
 *
 * @param player The player whose quest is being retrieved.
 * @param quest The name of the quest.
 * @return The Quest object representing the quest for the player.
 */
fun getQuest(
    player: Player,
    quest: String,
): Quest = player.questRepository.getQuest(quest)

/**
 * Starts a quest for a player if they meet the necessary requirements.
 *
 * @param player The player who is starting the quest.
 * @param quest The name of the quest to start.
 * @return True if the quest was successfully started, false if the player doesn't meet the requirements.
 */
fun startQuest(
    player: Player,
    quest: String,
): Boolean {
    val quest = player.questRepository.getQuest(quest)
    val canStart = quest.hasRequirements(player)
    if (!canStart) return false
    quest.start(player)
    return true
}

/**
 * Finishes a quest for a player, marking it as complete.
 *
 * @param player The player who is finishing the quest.
 * @param quest The name of the quest to finish.
 */
fun finishQuest(
    player: Player,
    quest: String,
) {
    player.questRepository.getQuest(quest).finish(player)
}

/**
 * Checks if a player has completed a specific quest before allowing them to proceed with another quest.
 * Sends a message to the player if they haven't completed the required quest.
 *
 * @param player The player whose quest completion status is being checked.
 * @param questName The name of the required quest.
 * @param message The message to send to the player if they have not completed the required quest.
 * @return True if the player has completed the required quest, otherwise false.
 */
fun requireQuest(
    player: Player,
    questName: String,
    message: String,
): Boolean {
    if (!isQuestComplete(player, questName)) {
        sendMessage(player, "You must have completed the $questName quest. $message")
        return false
    }
    return true
}

/**
 * Checks if a player has met the requirements to start a specific quest.
 *
 * @param player The player whose quest requirements are being checked.
 * @param quest The name of the quest.
 * @param message If true, a message will be sent to the player if they do not meet the requirements.
 * @return True if the player meets the requirements for the quest, otherwise false.
 */
@JvmOverloads
fun hasRequirement(
    player: Player,
    quest: String,
    message: Boolean = true,
): Boolean {
    val questReq = QuestRequirements.values().filter { it.questName.equals(quest, true) }.firstOrNull() ?: return false
    return core.api.hasRequirement(player, QuestReq(questReq), message)
}

/**
 * Checks if the player has started the given quest.
 *
 * @param player The player whose quest status to check.
 * @param quest The name or identifier of the quest.
 * @return `true` if the player has started the quest, `false` otherwise.
 */
fun hasStarted(player: Player, quest: String): Boolean {
    return player.getQuestRepository().hasStarted(quest)
}

/**
 * Checks if the specified entity has finished its movement.
 *
 * @param entity The entity whose movement status is being checked.
 * @return True if the entity has finished moving, otherwise false.
 */
fun finishedMoving(entity: Entity): Boolean = entity.clocks[Clocks.MOVEMENT] < GameWorld.ticks

/**
 * Truncates the movement path of an entity towards a destination location if the distance is too far.
 *
 * @param mover The entity (either player or NPC) that is moving.
 * @param destination The target destination location to move towards.
 * @return A pair where the first value indicates whether the path was truncated,
 *         and the second value is the new truncated destination location.
 */
fun truncateLoc(
    mover: Entity,
    destination: Location,
): Pair<Boolean, Location> {
    val vector = Vector.betweenLocs(mover.location, destination)
    val normVec = vector.normalized()
    val mag = vector.magnitude()

    var multiplier = if (mover is NPC) 14.0 else ServerConstants.MAX_PATHFIND_DISTANCE.toDouble()
    var clampedMultiplier = min(multiplier, mag)

    var truncated = multiplier == clampedMultiplier

    return Pair(truncated, mover.location.transform(normVec * clampedMultiplier))
}

/**
 * Produces a ground item at the player's location.
 *
 * @param player The player for whom the ground item will be produced.
 * @param item The item id of the ground item to be created.
 */
fun produceGroundItem(
    player: Player,
    item: Int,
) {
    GroundItemManager.create(Item(item), player)
}

/**
 * Produces a ground item at a specified location.
 *
 * @param owner The player who owns the ground item, or null if the item has no owner.
 * @param id The item id of the ground item to be created.
 * @param amount The quantity of the item to be created.
 * @param location The location where the ground item will be placed.
 * @return The created [GroundItem] at the specified location.
 */
fun produceGroundItem(
    owner: Player?,
    id: Int,
    amount: Int,
    location: Location,
): GroundItem = GroundItemManager.create(Item(id, amount), location, owner)

/**
 * Removes a ground item from the game world.
 *
 * @param node The [GroundItem] to be removed from the game world.
 */
fun removeGroundItem(node: GroundItem) {
    GroundItemManager.destroy(node)
}

/**
 * Checks if a ground item is valid and exists in the game world.
 *
 * @param node The [GroundItem] to be checked for validity.
 * @return True if the ground item exists in the world, otherwise false.
 */
fun isValidGroundItem(node: GroundItem): Boolean = GroundItemManager.getItems().contains(node)

/**
 * Retrieves the item definition for a given item id.
 *
 * @param id The id for the item.
 * @return The [ItemDefinition] associated with the provided item id.
 */
fun itemDefinition(id: Int): ItemDefinition = ItemDefinition.forId(id)

/**
 * Checks if all specified items are present in the player's inventory.
 *
 * @param player The player whose inventory is being checked.
 * @param ids The ids of the items to check for in the inventory.
 * @return True if all specified items are present in the inventory, otherwise false.
 */
fun allInInventory(
    player: Player,
    vararg ids: Int,
): Boolean =
    ids.all { id ->
        inInventory(player, id)
    }

/**
 * Retrieves the name of the NPC based on its id.
 *
 * @param id The NPC id.
 * @return The name of the NPC as a String.
 */
fun getNPCName(id: Int): String = NPCDefinition.forId(id).name

/**
 * Decants items in a container, organizing them by dosage and creating new items with the correct dosage.
 * It removes the original items and adds the newly decanted items to the container.
 *
 * @param container The container to decant the items from.
 * @return A pair of lists where:
 *  - The first list contains the items to be removed from the container.
 *  - The second list contains the items to be added to the container.
 */
fun decantContainer(container: core.game.container.Container): Pair<List<Item>, List<Item>> {
    val doseCount = HashMap<Consumable, Int>()
    val toRemove = ArrayList<Item>()
    val toAdd = ArrayList<Item>()
    val doseRegex = Pattern.compile("(\\([0-9]\\))")

    for (item in container.toArray()) {
        if (item == null) continue
        val potionType = Consumables.getConsumableById(item.id)?.consumable as? Potion ?: continue
        val matcher = doseRegex.matcher(item.name)
        if (!matcher.find()) continue
        val dosage =
            matcher
                .group(1)
                .replace("(", "")
                .replace(")", "")
                .toIntOrNull() ?: continue
        doseCount[potionType] = (doseCount[potionType] ?: 0) + dosage
        toRemove.add(item)
    }

    for ((consumable, count) in doseCount) {
        val maxDose = consumable.ids.size
        val totalMaxDosePotions = count / maxDose
        val remainderDose = count % maxDose
        if (totalMaxDosePotions > 0) toAdd.add(Item(consumable.ids[0], totalMaxDosePotions))
        if (remainderDose > 0) toAdd.add(Item(consumable.ids[consumable.ids.size - remainderDose]))
    }

    val emptyVials = toRemove.size - toAdd.sumOf { it.amount }
    if (emptyVials > 0) toAdd.add(Item(229, emptyVials))
    return Pair<List<Item>, List<Item>>(toRemove, toAdd)
}

/**
 * Opens the shop associated with a given NPC for a player.
 *
 * @param player The player who is opening the shop.
 * @param npc The NPC whose shop should be opened.
 * @return True if the shop was successfully opened, false otherwise.
 */
fun openNpcShop(
    player: Player,
    npc: Int,
): Boolean {
    val shop = Shops.shopsByNpc[npc]

    if (shop != null) {
        shop.openFor(player)
        return true
    }

    return false
}

/**
 * Retrieves the Slayer Master assigned to the player.
 *
 * @param player The player whose Slayer Master is being retrieved.
 * @return The [NPC] representing the player's Slayer Master.
 */
fun getSlayerMaster(player: Player): NPC = findNPC(SlayerManager.getInstance(player).master?.npc as Int) as NPC

/**
 * Retrieves the location of the player's assigned Slayer Master.
 *
 * @param player The player whose Slayer Master location is being retrieved.
 * @return A string representing the location of the Slayer Master.
 */
fun getSlayerMasterLocation(player: Player): String =
    when (getSlayerMaster(player).id) {
        NPCs.CHAELDAR_1598 -> "Zanaris"
        NPCs.DURADEL_8275 -> "Shilo Village"
        NPCs.MAZCHNA_8274 -> "Canifis"
        NPCs.TURAEL_8273 -> "Taverley"
        NPCs.VANNAKA_1597 -> "Edgeville Dungeon"
        else -> "The Backrooms"
    }

/**
 * Retrieves the player's current Slayer task.
 *
 * @param player The player whose Slayer task is being retrieved.
 * @return The active [Tasks] assigned to the player, or null if no task is assigned.
 */
fun getSlayerTask(player: Player): Tasks? = SlayerManager.getInstance(player).activeTask

/**
 * Checks if the player has an active Slayer task.
 *
 * @param player The player whose task status is being checked.
 * @return True if the player has an active Slayer task, otherwise false.
 */
fun hasSlayerTask(player: Player): Boolean = SlayerManager.getInstance(player).hasTask()

/**
 * Retrieves the name of the player's current Slayer task.
 *
 * @param player The player whose Slayer task name is being retrieved.
 * @return A string representing the name of the Slayer task.
 */
fun getSlayerTaskName(player: Player): String = SlayerManager.getInstance(player).taskName

/**
 * Retrieves the number of kills remaining for the player's current Slayer task.
 *
 * @param player The player whose remaining kills are being retrieved.
 * @return An int representing the number of kills left to complete the Slayer task.
 */
fun getSlayerTaskKillsRemaining(player: Player): Int = SlayerManager.getInstance(player).amount

/**
 * Retrieves the task flags for the player's current Slayer task.
 *
 * @param player The player whose task flags are being retrieved.
 * @return An int representing the task flags.
 */
fun getSlayerTaskFlags(player: Player): Int = SlayerManager.getInstance(player).flags.taskFlags

/**
 * Provides a tip or hint about the player's current Slayer task.
 *
 * @param player The player whose Slayer task tip is being retrieved.
 * @return An array of string containing the Slayer task tip.
 */
fun getSlayerTip(player: Player): Array<out String> =
    if (hasSlayerTask(player)) {
        getSlayerTask(player)?.tip!!
    } else {
        arrayOf("You need something new to hunt.")
    }

/**
 * Transforms an NPC into another NPC for a specified duration before reverting back.
 *
 * @param npc The [NPC] to be transformed.
 * @param transformTo The id of the NPC to transform into.
 * @param restoreTicks The number of game ticks before the NPC reverts to its original form.
 */
fun transformNpc(
    npc: NPC,
    transformTo: Int,
    restoreTicks: Int,
) {
    npc.transform(transformTo)
    Pulser.submit(
        object : Pulse(restoreTicks) {
            override fun pulse(): Boolean {
                npc.reTransform()
                return true
            }
        },
    )
}

/**
 * Checks if the player has any awaiting Grand Exchange collections.
 *
 * @param player The player whose Grand Exchange records are being checked.
 * @return true if there is at least one offer with an item awaiting collection, otherwise false.
 */
fun hasAwaitingGrandExchangeCollections(player: Player): Boolean {
    val records = ExchangeHistory.getInstance(player)

    for (record in records.offerRecords) {
        val offer = records.getOffer(record)

        return offer != null && offer.withdraw[0] != null
    }

    return false
}

/**
 * Opens the Grand Exchange collection box for the player.
 *
 * @param player The player who is attempting to open the Grand Exchange collection box.
 */
fun openGrandExchangeCollectionBox(player: Player) {
    ExchangeHistory.getInstance(player).openCollectionBox()
}

/**
 * Opens the player's bank account interface.
 *
 * @param player The player who is attempting to open the bank account.
 */
fun openBankAccount(player: Player) {
    player.bank.open()
}

/**
 * Opens the player's deposit box interface.
 *
 * @param player The player who is attempting to open the deposit box.
 */
fun openDepositBox(player: Player) {
    player.bank.openDepositBox()
}

/**
 * Opens the player's bank pin settings.
 *
 * @param player The player who is attempting to open the bank pin settings.
 */
fun openBankPinSettings(player: Player) {
    player.bankPinManager.openSettings()
}

/**
 * Opens the Grand Exchange interface for the player.
 *
 * @param player The player who is attempting to access the Grand Exchange.
 */
fun openGrandExchange(player: Player) {
    StockMarket.openFor(player)
}

/**
 * Opens a door and instantly moves the player through it.
 *
 * @param player The player interacting with the door.
 * @param node The scenery object representing the door to be opened.
 */
fun openDoor(player: Player, node: Scenery) {
    val destination = DoorActionHandler.getEndLocation(player, node, true)

    DoorActionHandler.open(
        node,
        node,
        node.id + 1,
        node.id + 1,
        true,
        3,
        false
    )
    forceWalk(player, destination, "")
}

/**
 * Gets the name of a scenery.
 *
 * @param id The id for the scenery object.
 * @return The name.
 */
fun getSceneryName(id: Int): String = SceneryDefinition.forId(id).name

/**
 * Cures the poison effect from an entity if it is currently poisoned.
 *
 * @param entity The entity to cure of poison.
 */
fun curePoison(entity: Entity) {
    if (!hasTimerActive<Poison>(entity)) return
    removeTimer<Poison>(entity)
    if (entity is Player) sendMessage(entity, "Your poison has been cured.")
}

/**
 * Cures the disease effect from an entity.
 *
 * @param entity The entity to cure of disease.
 */
fun cureDisease(entity: Entity) {
    if (!hasTimerActive<Disease>(entity)) return
    removeTimer<Disease>(entity)
    if (entity is Player) sendMessage(entity, "Your disease has been cured.")
}

/**
 * Checks if an entity is currently poisoned.
 *
 * @param entity The entity to check for poison.
 * @return True if the entity is poisoned, otherwise false.
 */
fun isPoisoned(entity: Entity): Boolean = getTimer<Poison>(entity) != null

/**
 * Checks if an entity is currently diseased.
 *
 * @param entity The entity to check for disease.
 * @return True if the entity is diseased, otherwise false.
 */
fun isDiseased(entity: Entity): Boolean = getTimer<Disease>(entity) != null

/**
 * Checks if an entity is currently stunned.
 *
 * @param entity The entity to check for stun status.
 * @return True if the entity is stunned, otherwise false.
 */
fun isStunned(entity: Entity): Boolean = entity.clocks[Clocks.STUN] >= getWorldTicks()

/**
 * Applies a poison effect to an entity, or refreshes the existing poison effect if already active.
 *
 * @param entity The entity to apply or refresh the poison effect on.
 * @param source The entity responsible for applying the poison (e.g., attacker or source of poison).
 * @param severity The number of remaining hits for the poison effect.
 */
fun applyPoison (entity: Entity, source: Entity, severity: Int) {
    if(hasTimerActive<PoisonImmunity>(entity)) {
        return
    }
    if(entity.isPoisonImmune()) {
        return
    }
    val existingTimer = getTimer<Poison>(entity)

    if (existingTimer != null) {
        if (existingTimer.severity > severity) {
            return
        } else {
            existingTimer.severity = severity
            existingTimer.damageSource = source
        }
    } else {
        registerTimer(entity, spawnTimer<Poison>(source, severity))
    }
}

/**
 * Applies a disease effect to an entity, or refreshes the existing disease effect if already active.
 *
 * @param entity The entity to apply or refresh the disease effect on.
 * @param source The entity responsible for applying the disease (e.g., attacker or source of disease).
 * @param hits The number of remaining hits for the disease effect.
 */
fun applyDisease(entity: Entity, source: Entity, hits: Int) {
    if (hasTimerActive<Disease>(entity)) return
    registerTimer(entity, spawnTimer<Disease>(source, hits))
}

/**
 * Updates the player's credit balance by adding the specified amount.
 *
 * @param player The player whose credits are being updated.
 * @param amount The amount to adjust the player's credits by. Positive values add credits, negative values subtract.
 * @return true if the update was successful, false if the resulting balance would be negative.
 */
fun updateCredits(
    player: Player,
    amount: Int,
): Boolean {
    val creds = getCredits(player) + amount

    if (creds < 0) {
        return false
    } else {
        player.details.accountInfo.credits = creds
    }

    return true
}

/**
 * Gets the current credit balance of the player.
 *
 * @param player The player whose credit balance is being retrieved.
 * @return The player's current credit balance.
 */
fun getCredits(player: Player): Int = player.details.accountInfo.credits


/**
 * Calculates the maximum damage a player or entity will take from a dragon's fire breath,
 * considering the presence of protection methods such as dragonfire shields, potions, and prayers.
 *
 * This function applies damage reduction based on the player's or entity's protection
 * from dragonfire. The final damage is calculated by reducing the maximum possible damage
 * according to whether the entity has protection from fire breath via shield, potion, or prayer.
 * If no protection is available, the full damage is taken.
 *
 * @param entity The entity (either player or NPC) receiving the dragonfire attack.
 * @param maxDamage The maximum potential damage that the dragonfire breath can cause.
 * @param wyvern A flag indicating if the dragonfire is from a wyvern. Defaults to false (meaning it's regular dragonfire).
 * @param unprotectableDamage The portion of damage that cannot be reduced by any protection. Defaults to 0.
 * @param sendMessage A flag to send a message to the entity about the protection effect. Defaults to false.
 * @return The final calculated damage after applying the protection effects.
 *         If protection is present, it will reduce the damage accordingly.
 */
fun calculateDragonFireMaxHit(
    entity: Entity,
    maxDamage: Int,
    wyvern: Boolean = false,
    unprotectableDamage: Int = 0,
    sendMessage: Boolean = false,
): Int {
    val hasShield: Boolean
    val hasPotion: Boolean
    val hasPrayer: Boolean

    if (entity is Player) {
        // Check for player-specific protection methods.
        hasShield = hasDragonfireShieldProtection(entity, wyvern)
        hasPotion = !wyvern && hasTimerActive<DragonFireImmunity>(entity)
        hasPrayer = entity.prayer.get(PrayerType.PROTECT_FROM_MAGIC)

        if (sendMessage) {
            var message = "You are horribly burnt by the ${if (wyvern) "icy" else "fiery"} breath."
            if (hasShield && hasPotion) {
                message =
                    "Your potion and shield fully absorb the ${if (wyvern) "icy" else "fiery"} breath."
            } else if (hasShield) {
                message = "Your shield absorbs most of the ${if (wyvern) "icy" else "fiery"} breath."
            } else if (hasPotion) {
                message = "Your potion absorbs some of the fiery breath."
            } else if (hasPrayer) {
                message = "Your prayer absorbs some of the ${if (wyvern) "icy" else "fiery"} breath."
            }
            sendMessage(entity, message)
        }
    } else {
        // Check for NPC-specific protection methods.
        val dragonFireTokens = entity.getDragonfireProtection(!wyvern)
        hasShield = dragonFireTokens and 0x2 != 0
        hasPotion = dragonFireTokens and 0x4 != 0
        hasPrayer = dragonFireTokens and 0x8 != 0
    }

    var effectiveDamage = maxDamage.toDouble()
    if (hasPrayer && !hasShield && !hasPotion) {
        effectiveDamage -= 0.6 * maxDamage
    } else {
        if (hasShield) effectiveDamage -= 0.9 * maxDamage
        if (hasPotion) effectiveDamage -= 0.1 * maxDamage
    }

    return Math.max(unprotectableDamage, effectiveDamage.toInt())
}

/**
 * Checks if the player has protection against dragonfire or wyvern attacks based on their equipped shield.
 *
 * @param player The player whose equipment is being checked for protection.
 * @param wyvern If true, checks for protection against wyvern attacks; otherwise, checks for dragonfire protection.
 * @return True if the player has the appropriate shield for protection, otherwise false.
 */
fun hasDragonfireShieldProtection(
    player: Player,
    wyvern: Boolean = false,
): Boolean {
    val shield = getItemFromEquipment(player, EquipmentSlot.SHIELD) ?: return false
    return when (shield.id) {
        Items.ELEMENTAL_SHIELD_2890, Items.MIND_SHIELD_9731 -> wyvern
        Items.ANTI_DRAGON_SHIELD_1540 -> !wyvern
        Items.DRAGONFIRE_SHIELD_11283, Items.DRAGONFIRE_SHIELD_11284 -> true
        else -> false
    }
}

/**
 * Gets an existing timer for the specified entity and identifier, or starts a new one if none exists.
 *
 * @param entity The entity for which the timer is being retrieved or created.
 * @param identifier The unique identifier for the timer.
 * @param args The additional arguments to pass when creating the timer, if needed.
 * @return The existing timer if one exists, or the newly created timer if no existing timer is found.
 */
fun getOrStartTimer(
    entity: Entity,
    identifier: String,
    vararg args: Any,
): RSTimer? {
    val existing = getTimer(entity, identifier)
    if (existing != null) return existing
    return spawnTimer(identifier, *args).also { registerTimer(entity, it) }
}

/**
 * Sends interface settings (access masks) to the player.
 *
 * @param player The player to whom the settings will be sent.
 * @param settingsHash The hash representing the settings.
 * @param childId The child id of the interface component.
 * @param interfaceId The id of the interface.
 * @param offset The offset within the component.
 * @param length The length of the settings block.
 */
fun sendIfaceSettings(
    player: Player,
    settingsHash: Int,
    childId: Int,
    interfaceId: Int,
    offset: Int,
    length: Int
) {
    PacketRepository.send(AccessMask::class.java, AccessMaskContext(player, settingsHash, childId, interfaceId, offset, length))
}

/**
 * Displays a quest reward item model on the quest completion interface.
 *
 * @param player The player to display the item to.
 * @param itemId The item id to display.
 * @param zoom The zoom level for the item model (default = 240).
 */
fun displayQuestItem(
    player: Player,
    itemId: Int,
    zoom: Int = 240
) {
    player.packetDispatch.sendItemZoomOnInterface(
        itemId,
        zoom,
        Components.QUEST_COMPLETE_SCROLL_277,
        5
    )
}

/**
 * Updates the torso's appearance for the given player.
 *
 * @param player The player whose torso look is being updated.
 * @param lookId The id of the new torso look.
 */
fun updateTorsoLook(player: Player, lookId: Int) {
    player.appearance.torso.changeLook(lookId)
}

/**
 * Updates the torso's color for the given player.
 *
 * @param player The player whose torso color is being updated.
 * @param colorId The id of the new torso color.
 */
fun updateTorsoColor(player: Player, colorId: Int) {
    player.appearance.torso.changeColor(colorId)
}

/**
 * Updates the arms' appearance for the given player.
 *
 * @param player The player whose arms look is being updated.
 * @param lookId The id of the new arms look.
 */
fun updateArmsLook(player: Player, lookId: Int) {
    player.appearance.arms.changeLook(lookId)
}

/**
 * Updates the arms' color for the given player.
 *
 * @param player The player whose arms color is being updated.
 * @param colorId The id of the new arms color.
 */
fun updateArmsColor(player: Player, colorId: Int) {
    player.appearance.arms.changeColor(colorId)
}

/**
 * Updates the legs' appearance for the given player.
 *
 * @param player The player whose legs look is being updated.
 * @param lookId The id of the new legs look.
 */
fun updateLegsLook(player: Player, lookId: Int) {
    player.appearance.legs.changeLook(lookId)
}

/**
 * Updates the legs' color for the given player.
 *
 * @param player The player whose legs color is being updated.
 * @param colorId The id of the new legs color.
 */
fun updateLegsColor(player: Player, colorId: Int) {
    player.appearance.legs.changeColor(colorId)
}

/**
 * Updates the wrists' appearance for the given player.
 *
 * @param player The player whose wrists look is being updated.
 * @param lookId The id of the new wrists look.
 */
fun updateWristsLook(player: Player, lookId: Int) {
    player.appearance.wrists.changeLook(lookId)
}

/**
 * Updates the hair's appearance for the given player.
 *
 * @param player The player whose hair look is being updated.
 * @param lookId The id of the new hair look.
 */
fun updateHairLook(player: Player, lookId: Int) {
    player.appearance.hair.changeLook(lookId)
}

/**
 * Updates the hair's color for the given player.
 *
 * @param player The player whose hair color is being updated.
 * @param colorId The id of the new hair color.
 */
fun updateHairColor(player: Player, colorId: Int) {
    player.appearance.hair.changeColor(colorId)
}

/**
 * Updates the beard's appearance for the given player.
 *
 * @param player The player whose beard look is being updated.
 * @param lookId The id of the new beard look.
 */
fun updateBeardLook(player: Player, lookId: Int) {
    player.appearance.beard.changeLook(lookId)
}

/**
 * Updates the feet's color for the given player.
 *
 * @param player The player whose feet color is being updated.
 * @param colorId The id of the new feet color.
 */
fun updateFeetColor(player: Player, colorId: Int) {
    player.appearance.feet.changeColor(colorId)
}

/**
 * Updates the skin color for the given player.
 *
 * @param player The player whose skin color is being updated.
 * @param colorId The id of the new skin color.
 */
fun updateSkinColor(player: Player, colorId: Int) {
    player.appearance.skin.changeColor(colorId)
}

/**
 * Updates the look appearance of a player's body part.
 *
 * @param part The body part instance to update (e.g., player.appearance.torso).
 * @param lookId The id of the new look.
 */
fun updateLook(part: BodyPart, lookId: Int) {
    part.changeLook(lookId)
}

/**
 * Updates the color of a player's body part.
 *
 * @param part The body part instance to update (e.g., player.appearance.hair).
 * @param colorId The id of the new color.
 */
fun updateColor(part: BodyPart, colorId: Int) {
    part.changeColor(colorId)
}

private class ContentAPI
