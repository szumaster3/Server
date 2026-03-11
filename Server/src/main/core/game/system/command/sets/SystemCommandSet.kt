package core.game.system.command.sets

import content.data.items.ChargedItem
import core.api.EquipmentSlot
import core.api.runTask
import core.api.sendDialogue
import core.api.sendInputDialogue
import core.cache.def.impl.ItemDefinition
import core.game.dialogue.InputType
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.system.SystemManager
import core.game.system.SystemState
import core.game.system.command.Privilege
import core.game.world.GameWorld
import core.game.world.repository.Repository
import core.plugin.Initializable
import shared.consts.Items
import kotlin.system.exitProcess

@Initializable
class SystemCommandSet : CommandSet(Privilege.ADMIN) {

    override fun defineCommands() {
        /*
         * Command to start a server update with an optional countdown.
         */

        define(name = "update",
            privilege = Privilege.MODERATOR,
            usage = "::update",
            description = "Launch an update",
        ) { _, args ->
            if (args.size > 1) {
                SystemManager.updater.setCountdown(args[1].toInt())
            }
            SystemManager.flag(SystemState.UPDATING)
        }

        /*
         * Command to cancel a pending server update.
         */

        define(
            name = "cancelupdate",
            privilege = Privilege.MODERATOR,
            usage = "::cancelupdate",
            description = "WARNING: Case insensitive due to dialogue limitations."
        ) { _, _ ->
            SystemManager.updater.cancel()
        }

        /*
         * Command for a player to reset their own password.
         */
        define(
            name = "resetpassword",
            privilege = Privilege.MODERATOR,
            usage = "::resetpassword",
            description = "Case insensitive due to dialogue limitations.",
        ) { player, _ ->
            sendInputDialogue(player, InputType.STRING_SHORT, "Enter Current Password:") { value ->
                val pass = value.toString()
                runTask(player) {
                    if (GameWorld.authenticator.checkPassword(player, pass)) {
                        sendInputDialogue(player, InputType.STRING_SHORT, "Enter New Password:") { value2 ->
                            val newPass = value2.toString()
                            if (pass == newPass) {
                                sendDialogue(player, "Failed: Passwords Match")
                            } else if (newPass.length !in 5..20) {
                                sendDialogue(player, "Failed: Password Too Long Or Too Short")
                            } else if (newPass == player.details.accountInfo.username) {
                                sendDialogue(player, "Failed: Password Is Username")
                            } else {
                                GameWorld.authenticator.updatePassword(player.details.accountInfo.username, newPass)
                                sendDialogue(player, "Success: Password Updated!")
                            }
                        }
                    } else {
                        sendDialogue(player, "Fail: Wrong Password.")
                    }
                }
            }
        }

        /*
         * Command for admin to set a password for another user.
         */

        define(
            name = "setpasswordother",
            privilege = Privilege.ADMIN,
            usage = "::resetpasswordother <lt>USERNAME<gt> <lt>NEW<gt>",
            description = "Gives the username password NEW.",
        ) { player, args ->
            if (args.size != 3) {
                reject(
                    player,
                    "Usage: ::resetpasswordother user new",
                    "WARNING: THIS IS PERMANENT.",
                    "WARNING: PASSWORD CAN NOT CONTAIN SPACES.",
                )
            }
            val otherUser = args[1]
            val newPass = args[2]

            if (GameWorld.accountStorage.checkUsernameTaken(otherUser)) {
                if (newPass.length < 5 || newPass.length > 20) {
                    reject(player, "NEW PASSWORD MUST BE BETWEEN 5 AND 20 CHARACTERS")
                }

                if (newPass == otherUser) {
                    reject(player, "PASSWORD CAN NOT BE SAME AS USERNAME.")
                }

                GameWorld.authenticator.updatePassword(otherUser, newPass)
                notify(player, "Password updated successfully.")
            } else {
                reject(player, "USER DOES NOT EXIST!")
            }
        }

        /*
         * Command to give an item to a player.
         */

        define(
            name = "giveitem",
            privilege = Privilege.ADMIN,
            usage = "::giveitem <lt>USERNAME<gt> <lt>ITEM ID<gt> <lt>AMOUNT<gt>",
            description = "Gives the user the amount of the given item.",
        ) { player, args ->
            if (args.size == 3 || args.size == 4) {
                val victim = Repository.getPlayerByName(args[1])
                val itemID = args[2].toIntOrNull()
                val amount = args.getOrNull(3)?.toIntOrNull() ?: 1

                if (victim == null) {
                    reject(player, "INVALID TARGET USERNAME.")
                }

                if (itemID == null || itemID <= 0 || itemID > ItemDefinition.getDefinitions().size) {
                    reject(player, "INVALID ITEM ID ENTERED.")
                }

                if (amount > Int.MAX_VALUE || amount <= 0) {
                    reject(player, "INVALID ITEM ID ENTERED.")
                }

                val item = Item(itemID!!, amount)
                val invFull = victim!!.inventory.isFull
                val syntax = if (amount > 1) "items" else "item"

                if (invFull) {
                    victim.bank.add(item)
                } else {
                    victim.inventory.add(item)
                }

                notify(
                    player,
                    "Successfully gave ${victim.username} $amount ${item.name}. ${if (invFull) "The $syntax were sent to their bank." else ""}",
                )
                notify(
                    victim,
                    "You received $amount ${item.name} from ${player.username}. ${if (invFull) "The $syntax were placed in your bank." else ""}",
                )
            } else {
                reject(player, "WRONG USAGE. USE giveitem target itemID || giveitem target itemID amt")
            }
        }

        /*
         * Command to remove an item from a
         * specific location (inventory, bank, equipment) of a player.
         */

        define(
            name = "removeitem",
            privilege = Privilege.ADMIN,
            usage = "::removeitem <lt>LOC<gt> <lt>USERNAME<gt> <lt>ITEM ID<gt> <lt>AMOUNT<gt>",
            description = "LOC = bank,inventory,equipment",
        ) { player, args ->
            if (args.size == 4 || args.size == 5) {
                val itemLoc = args[1].lowercase()
                val victim = Repository.getPlayerByName(args[2])
                val itemID = args[3].toIntOrNull()
                var amount = args.getOrNull(4)?.toIntOrNull() ?: 1

                if (victim == null) {
                    reject(player, "INVALID TARGET USERNAME.")
                }

                if (itemID == null || itemID <= 0 || itemID > ItemDefinition.getDefinitions().size) {
                    reject(player, "INVALID ITEM ID ENTERED.")
                }

                if (amount > Int.MAX_VALUE || amount <= 0) {
                    reject(player, "INVALID ITEM AMOUNT ENTERED.")
                }

                val item = Item(itemID!!, amount)
                var totalItemAmount = 0

                when (itemLoc) {
                    "i", "inv", "inventory" -> {
                        totalItemAmount = victim!!.inventory.getItem(item).amount
                        victim.inventory.remove(item)
                    }

                    "b", "bk", "bank" -> {
                        totalItemAmount = victim!!.bank.getItem(item).amount
                        victim.bank.remove(item)
                    }

                    "e", "equip", "equipment" -> {
                        totalItemAmount = victim!!.equipment.getItem(item).amount
                        victim.equipment.remove(item)
                    }

                    else ->
                        reject(
                            player,
                            "INVALID ITEM LOCATION ENTERED. USE: ",
                            "i, inv, inventory | b, bk, bank | e, equip, equipment",
                        )
                }

                if (amount > totalItemAmount) {
                    amount = totalItemAmount
                }

                notify(player, "Successfully removed $amount ${item.name} from ${victim!!.username}.")
                notify(victim, "${player.username} removed $amount ${item.name} from your inventory.")
            } else {
                reject(
                    player,
                    "WRONG USAGE. USE removeitem itemLoc target itemID || removeitem itemLoc target itemID amt",
                    "ItemLoc: inv = inventory | equip = equipment | bank |",
                )
            }
        }

        /*
         * Command to remove all instances of a given
         * item from a player's account (inventory, bank, equipment).
         */

        define(
            name = "removeitemall",
            privilege = Privilege.ADMIN,
            usage = "::removeitemall <lt>USERNAME<gt> <lt>ITEM ID<gt>",
            description = "Removes ALL of a given item from the player",
        ) { player, args ->
            if (args.size == 3) {
                val victim = Repository.getPlayerByName(args[1])
                val itemID = args[2].toIntOrNull()

                if (victim == null) {
                    reject(player, "INVALID TARGET USERNAME.")
                }

                if (itemID == null || itemID <= 0 || itemID > ItemDefinition.getDefinitions().size) {
                    reject(player, "INVALID ITEM ID ENTERED.")
                }

                val itemInv = Item(itemID!!)
                val itemNote = Item(itemInv.noteChange)

                val untotal =
                    victim!!.inventory.getAmount(itemID) + victim.bank.getAmount(itemID) +
                        victim.equipment.getAmount(
                            itemID,
                        )

                val nototal =
                    victim.inventory.getAmount(itemNote) + victim.bank.getAmount(itemNote) +
                        victim.equipment.getAmount(
                            itemNote,
                        )

                val eqtotal = victim.equipment.getAmount(itemID) + victim.equipment.getAmount(itemNote)

                val total = untotal + nototal + eqtotal

                if (total == 0) {
                    reject(player, "USER HAS NONE OF THOSE ITEMS.")
                }

                victim.inventory.remove(Item(itemID, victim.inventory.getAmount(itemID)))
                victim.bank.remove(Item(itemID, victim.bank.getAmount(itemID)))
                victim.equipment.remove(Item(itemID, victim.equipment.getAmount(itemID)))

                victim.inventory.remove(Item(itemInv.noteChange, victim.inventory.getAmount(itemNote)))
                victim.bank.remove(Item(itemInv.noteChange, victim.bank.getAmount(itemNote)))
                victim.equipment.remove(Item(itemInv.noteChange, victim.equipment.getAmount(itemNote)))

                notify(player, "Successfully removed $total ${itemInv.name} from ${victim.username}.")
                notify(victim, "${player.username} removed $total ${itemInv.name} from your account.")
            } else {
                reject(player, "WRONG USAGE. USE removeitemall target itemID")
            }
        }

        /*
         * Command to give the player a rotten potato (admin item).
         */

        define(
            name = "potato",
            privilege = Privilege.ADMIN,
            usage = "::potato",
            description = "Gives you a rotten potato",
        ) { player, _ ->
            player.inventory.add(Item(Items.ROTTEN_POTATO_5733))
        }

        /*
         * Command to shut down the server immediately.
         */

        define(
            name = "shutdown",
            privilege = Privilege.ADMIN,
            usage = "::shutdown",
            description = "Shut down the server immediately",
        ) { _, _ ->
            exitProcess(0)
        }

        /*
         * Command to get or set the charge of an item,
         * either internal or distinct.
         */

        define(
            name = "charge",
            privilege = Privilege.ADMIN,
            usage = "::charge <lt>equipment slot name | item id<gt> [sd] [<lt>charge<gt>]",
            description = "Get/set the charge of an item. Flags: s = set, d = distinct(#).",
        ) { player, args ->
            if (args.size < 2) {
                reject(
                    player,
                    "Usage: ::charge <lt>equipment slot name | item id<gt> [sd] [<lt>charge<gt>]",
                    "Get internal: ::charge 4718",
                    "Get distinct: ::charge ring d",
                    "Set internal: ::charge head s 500",
                    "Set distinct: ::charge 1704 sd 4",
                )
            }

            @Suppress("UNCHECKED_CAST")
            val itemInfo =
                (
                    args[1].toIntOrNull()?.let {
                        getItemAndContainer(player, it) ?: reject(player, "Item not found: $it.")
                    } ?: EquipmentSlot.slotByName(args[1])?.ordinal?.let {
                        Pair(
                            player.equipment.get(it) ?: reject(player, "No item equipped at slot: ${args[1]}."),
                            player.equipment,
                        )
                    } ?: reject(player, "No slot: ${args[1]}.")
                ) as Pair<Item, core.game.container.Container>

            val item = itemInfo.first

            when (val flags = args.getOrElse(2) { "" }) {
                "" -> { // get internal charge
                    notify(player, "${item.name}: ${item.charge} charge.")
                }

                "d" -> { // get distinct charge
                    ChargedItem.forId(item.id)?.run {
                        notify(player, "${item.name}: (${ChargedItem.getCharge(item.id)}) charge.")
                    } ?: reject(player, "${item.name} is not a distinct(#) item.")
                }

                "s", "sd", "ds" -> {
                    if (args.size < 4) reject(player, "Must enter a charge value.")
                    val charge = (args[3].toIntOrNull() ?: reject(player, "Charge must be an integer.")) as Int
                    if (flags == "s") { // set internal charge
                        if (charge < 1) reject(player, "Charge must be a positive integer.")
                        item.charge = charge
                        notify(player, "Set internal charge of ${item.name} to $charge.")
                    } else { // set distinct charge
                        ChargedItem.forId(item.id)?.forCharge(charge)?.let {
                            itemInfo.second.replace(Item(it), item.slot)
                            notify(player, "Set distinct charge of ${item.name} to (${ChargedItem.getCharge(it)}).")
                        } ?: reject(player, "${item.name} is not a distinct(#) item.")
                    }
                }

                else -> {
                    reject(player, "Please enter valid flags: no flag/empty, s, d, sd.", "Use ::charge for examples.")
                }
            }
        }
    }

    private fun getItemAndContainer(
        player: Player,
        id: Int,
    ): Pair<Item, core.game.container.Container>? {
        if (id !in 0 until ItemDefinition.getDefinitions().size) return null
        arrayOf(player.inventory, player.equipment, player.bank).forEach { container ->
            container.toArray().firstOrNull { it?.id == id }?.let { return Pair(it, container) }
        }
        return null
    }
}
