package core.game.world.map.zone.impl

import content.data.GameAttributes
import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.combat.ImpactHandler
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.GameWorld.ticks
import core.game.world.map.zone.MapZone
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneBuilder
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Regions

/**
 * Represents the Desert zone.
 */
@Initializable
class DesertZone : MapZone(DESERT_ZONE, true), Plugin<Any?> {

    override fun newInstance(arg: Any?): Plugin<Any?> {
        ZoneBuilder.configure(this)
        return this
    }

    override fun fireEvent(identifier: String, vararg args: Any): Any? = null

    override fun configure() {
        register(getRegionBorders(Regions.DESERT_12589))
        register(getRegionBorders(Regions.DESERT_BANDITS_12590))
        register(getRegionBorders(Regions.DESERT_12591))
        register(getRegionBorders(Regions.DESERT_12843))
        register(getRegionBorders(Regions.DESERT_12844))
        register(getRegionBorders(Regions.DESERT_12845))
        register(getRegionBorders(Regions.DESERT_12847))
        register(getRegionBorders(Regions.DESERT_12848))
        register(getRegionBorders(Regions.DESERT_13100))
        register(getRegionBorders(Regions.DESERT_13101))
        register(getRegionBorders(Regions.DESERT_13102))
        register(getRegionBorders(Regions.DESERT_13103))
        register(getRegionBorders(Regions.DESERT_13355))
        register(getRegionBorders(Regions.DESERT_13356))
        register(getRegionBorders(Regions.DESERT_13357))
        register(getRegionBorders(Regions.DESERT_13359))
        register(getRegionBorders(Regions.DESERT_13360))
        register(getRegionBorders(Regions.DESERT_13361))
        register(getRegionBorders(Regions.DESERT_13611))
        register(getRegionBorders(Regions.DESERT_13612))
        register(getRegionBorders(Regions.DESERT_13613))
        register(getRegionBorders(Regions.DESERT_13614))
        register(getRegionBorders(Regions.DESERT_13615))
        register(getRegionBorders(Regions.DESERT_13616))
        register(getRegionBorders(Regions.DESERT_13617))
        register(getRegionBorders(Regions.DESERT_13872))
        register(getRegionBorders(Regions.DESERT_13873))
        register(ZoneBorders(3264, 3072, 3327, 3116))
        pulse.stop()
    }

    override fun enter(e: Entity): Boolean {
        if (e is Player) {
            val player = e
            if (!getAttribute(player, GameAttributes.TUTORIAL_COMPLETE, false)) {
                return true
            }
            player.setAttribute(DESERT_DELAY, ticks + getDelay(player))
            PLAYERS.add(player)
            if (!pulse.isRunning) {
                pulse.restart()
                pulse.start()
                Pulser.submit(pulse)
            }
        }
        return true
    }

    override fun leave(e: Entity, logout: Boolean): Boolean {
        if (e is Player) {
            PLAYERS.remove(e)
            e.removeAttribute(DESERT_DELAY)
        }
        return super.leave(e, logout)
    }

    companion object {
        private val WATER_SKINS = arrayOf(Item(Items.WATERSKIN4_1823), Item(Items.WATERSKIN3_1825), Item(Items.WATERSKIN2_1827), Item(Items.WATERSKIN1_1829))
        private val VESSILS = arrayOf(intArrayOf(Items.JUG_OF_WATER_1937, Items.JUG_1935), intArrayOf(Items.BUCKET_OF_WATER_1929, Items.BUCKET_1925), intArrayOf(Items.BOWL_OF_WATER_1921, Items.BOWL_1923), intArrayOf(Items.VIAL_OF_WATER_227, Items.VIAL_229), intArrayOf(Items.CUP_OF_WATER_4458, Items.EMPTY_CUP_1980))
        private val ANIMATION = Animation(Animations.EAT_OLD_829)
        private val PLAYERS: MutableList<Player> = ArrayList(20)
        private val DESERT_DELAY = "desert-delay"
        private val TUTORIAL_COMPLETE = GameAttributes.TUTORIAL_COMPLETE
        private val DESERT_ZONE = "Desert Zone"
        private val pulse: Pulse = object : Pulse(3) {
            override fun pulse(): Boolean {
                for (player in PLAYERS) {
                    if (!getAttribute(player, TUTORIAL_COMPLETE, false) || player.interfaceManager.isOpened() || player.interfaceManager.hasChatbox() || player.locks.isMovementLocked()) {
                        continue
                    }
                    if (player.getAttribute(DESERT_DELAY, -1) < ticks) {
                        effect(player)
                    }
                }
                return PLAYERS.isEmpty()
            }
        }

        private fun effect(player: Player) {
            player.setAttribute(DESERT_DELAY, ticks + getDelay(player))
            evaporate(player)
            if (drink(player)) {
                return
            }
            impact(player, RandomFunction.random(1, if (player.location.y < 2990) { 12 } else { 8 },), ImpactHandler.HitsplatType.NORMAL,)
            sendMessage(player, "You start dying of thirst while you're in the desert.")
        }

        fun evaporate(player: Player) {
            for (i in VESSILS.indices) {
                if (inInventory(player, VESSILS[i][0], 1)) {
                    if (removeItem(player, Item(VESSILS[i][0]))) {
                        addItem(player, VESSILS[i][1])
                        sendMessage(player, "The water in your " + getItemName(VESSILS[i][0]).lowercase().replace("of water", "").trim { it <= ' ' } + " evaporates in the desert heat.",)
                    }
                }
            }
        }

        fun drink(player: Player): Boolean {
            for (i in WATER_SKINS) {
                if (inInventory(player, i.id) && removeItem(player, i)) {
                    addItem(player, i.id + 2)
                    animate(player, ANIMATION)
                    sendMessage(player, "You take a drink of water.")
                    return true
                }
            }
            if (inInventory(player, Items.WATERSKIN0_1831, 1)) {
                sendMessage(player, "Perhaps you should fill up one of your empty waterskins.")
            } else {
                sendMessage(player, "You should get a waterskin for any travelling in the desert.")
            }
            return false
        }

        @JvmStatic
        private fun getDelay(player: Player): Int {
            var delay = 116
            if (inEquipment(player, Items.DESERT_SHIRT_1833, 1)) {
                delay += 17
            }
            if (inEquipment(player, Items.DESERT_ROBE_1835, 1)) {
                delay += 17
            }
            if (inEquipment(player, Items.DESERT_BOOTS_1837, 1)) {
                delay += 17
            }
            if (inEquipment(player, Items.DESERT_DISGUISE_4611, 1)) {
                delay += 12
            }

            val enchantedWaterTiara = inEquipment(player, Items.ENCHANTED_WATER_TIARA_11969)
            enchantedWaterTiara?.let { _ ->
                val charges = getCharge(Item(Items.ENCHANTED_WATER_TIARA_11969))
                delay += charges / 1000
            }

            return delay
        }
    }
}
