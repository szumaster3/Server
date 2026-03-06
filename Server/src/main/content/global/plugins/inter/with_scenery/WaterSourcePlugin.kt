package content.global.plugins.inter.with_scenery

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.link.diary.DiaryType
import core.game.system.task.Pulse
import core.game.world.map.Location
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery

/**
 * Handles filling water vessels from different water sources.
 */
class WaterSourcePlugin : InteractionListener {

    companion object {
        /**
         * Represents supported water source object ids.
         */
        private val WATER_SOURCES = intArrayOf(9695, Scenery.FOUNTAIN_153, Scenery.WELL_8927, Scenery.WELL_884, Scenery.WELL_878, Scenery.WELL_8747, Scenery.WELL_6549, Scenery.WELL_6097, Scenery.WELL_4005, Scenery.WELL_4004, Scenery.WELL_35671, Scenery.WELL_34576, Scenery.WELL_3359, Scenery.WELL_3305, Scenery.WELL_32024, Scenery.WELL_32023, Scenery.WELL_31359, Scenery.WELL_26945, Scenery.WELL_24166, Scenery.WELL_12897, Scenery.WELL_12201, Scenery.WELL_11793, Scenery.WATER_PUMP_15938, Scenery.WATER_PUMP_15937, Scenery.WATER_PUMP_15936, Scenery.WATERPUMP_6827, Scenery.WATERPUMP_34577, Scenery.WATERPUMP_24160, Scenery.WASHBASIN_14918, Scenery.SINK_9684, Scenery.SINK_9143, Scenery.SINK_874, Scenery.SINK_873, Scenery.SINK_8699, Scenery.SINK_6151, Scenery.SINK_4063, Scenery.SINK_40063, Scenery.SINK_37155, Scenery.SINK_37154, Scenery.SINK_36971, Scenery.SINK_35762, Scenery.SINK_34566, Scenery.SINK_34547, Scenery.SINK_34496, Scenery.SINK_34411, Scenery.SINK_34082, Scenery.SINK_33458, Scenery.SINK_29105, Scenery.SINK_26966, Scenery.SINK_25929, Scenery.SINK_25729, Scenery.SINK_24314, Scenery.SINK_24112, Scenery.SINK_22715, Scenery.SINK_20358, Scenery.SINK_16705, Scenery.SINK_16704, Scenery.SINK_15678, Scenery.SINK_14917, Scenery.SINK_14868, Scenery.SINK_13564, Scenery.SINK_13563, Scenery.SINK_12974, Scenery.SINK_12279, Scenery.SINK_10175, Scenery.PUMP_AND_TUB_13561, Scenery.PUMP_AND_DRAIN_23920, Scenery.PUMP_AND_DRAIN_13559, Scenery.LARGE_GEYSER_21355, Scenery.FOUNTAIN_880, Scenery.FOUNTAIN_879, Scenery.FOUNTAIN_6232, Scenery.FOUNTAIN_36781, Scenery.FOUNTAIN_34579, Scenery.FOUNTAIN_30820, Scenery.FOUNTAIN_30223, Scenery.FOUNTAIN_28662, Scenery.FOUNTAIN_2864, Scenery.FOUNTAIN_24265, Scenery.FOUNTAIN_24214, Scenery.FOUNTAIN_24161, Scenery.FOUNTAIN_22973, Scenery.FOUNTAIN_21764, Scenery.FOUNTAIN_11759, Scenery.FOUNTAIN_11007, Scenery.FOUNTAIN_10827, Scenery.FOUNTAIN_10437, Scenery.FOUNTAIN_10436, Scenery.FAIRY_FOUNTAIN_12089, Scenery.CARVED_FOUNTAIN_35469, 6249, 3264, 16302, 11661)
    }

    override fun defineListeners() {

        /*
         * Handles filling water vessels from sources.
         */

        onUseWith(IntType.SCENERY, WaterVessel.getValidInputs(), *WATER_SOURCES) { player, used, with ->
            val vessel = WaterVessel.fromItemId(used.id) ?: return@onUseWith false

            if (with.name.contains("well", ignoreCase = true) && !vessel.wellable) {
                sendMessage(
                    player,
                    buildMessage(used.name, with.name, "If I drop my @ down there, I don't think I'm likely to get it back.")
                )
                return@onUseWith true
            }

            if (with.id == 9695 && player.location.regionId != 11571) return@onUseWith false
            if (used.id in intArrayOf(434, 721, 722, 1980, 11151) && player.location.regionId != 11571) {
                return@onUseWith false
            }

            sendMessage(player, buildMessage(used.name, with.name,  "You fill the @ from the #."))

            if (vessel == WaterVessel.BUCKET && with.id == 11661) {
                player.achievementDiaryManager.getDiary(DiaryType.FALADOR)?.let { diary ->
                    if (!diary.isComplete(0, 7)) diary.updateTask(player, 0, 7, true)
                }
            }

            submitIndividualPulse(
                player,
                object : Pulse(1) {
                    override fun pulse(): Boolean {
                        if (removeItem(player, used.id)) {
                            animate(player, Animations.HUMAN_MULTI_USE_832)
                            addItemOrDrop(player, vessel.output)
                        }
                        return !vessel.autofill || amountInInventory(player, used.id) == 0
                    }
                }
            )
            return@onUseWith true
        }

        setDest(IntType.SCENERY, intArrayOf(9695), "use") { player, node ->
            if (player.location.regionId == 11571) {
                return@setDest Location(2934, 3280, 0)
            }
            return@setDest node.location
        }
    }

    /**
     * Builds a message replacing placeholders.
     * - `@` with the item name
     * - `#` with the scenery name
     */
    private fun buildMessage(itemName: String, sceneryName: String, template: String): String {
        val sb = StringBuilder()
        for (ch in template) {
            when (ch) {
                '@' -> {
                    for (nc in itemName) {
                        if (nc == '(') break else sb.append(nc.lowercaseChar())
                    }
                }
                '#' -> {
                    for (nc in sceneryName) {
                        if (nc == '(') break else sb.append(nc.lowercaseChar())
                    }
                }
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Represents vessels that can be filled with water.
     */
    internal enum class WaterVessel(val inputs: IntArray, val output: Int, val wellable: Boolean = false, val autofill: Boolean = true) {
        BUCKET(intArrayOf(Items.BUCKET_1925), Items.BUCKET_OF_WATER_1929, true),
        VIAL(intArrayOf(Items.VIAL_229), Items.VIAL_OF_WATER_227),
        JUG(intArrayOf(Items.JUG_1935), Items.JUG_OF_WATER_1937),
        BOWL(intArrayOf(Items.BOWL_1923), Items.BOWL_OF_WATER_1921),
        WATERING_CAN(intArrayOf(Items.WATERING_CAN_5331, Items.WATERING_CAN1_5333, Items.WATERING_CAN2_5334, Items.WATERING_CAN3_5335, Items.WATERING_CAN4_5336, Items.WATERING_CAN5_5337, Items.WATERING_CAN6_5338, Items.WATERING_CAN7_5339), Items.WATERING_CAN8_5340),
        WATER_SKIN(intArrayOf(Items.WATERSKIN0_1831, Items.WATERSKIN1_1829, Items.WATERSKIN2_1827, Items.WATERSKIN3_1825), Items.WATERSKIN4_1823),
        FISHBOWL(intArrayOf(Items.FISHBOWL_6667), Items.FISHBOWL_6668),
        CUP(intArrayOf(Items.EMPTY_CUP_1980), Items.CUP_OF_WATER_4458),
        KETTLE(intArrayOf(Items.KETTLE_7688), Items.FULL_KETTLE_7690),
        CLAY(intArrayOf(Items.CLAY_434), Items.SOFT_CLAY_1761, autofill = false),
        GOLDEN_BOWL(intArrayOf(Items.GOLD_BOWL_721), Items.GOLDEN_BOWL_723),
        BLESSED_GOLDEN_BOWL(intArrayOf(Items.BLESSED_GOLD_BOWL_722), Items.GOLDEN_BOWL_724),
        DREAM_VIAL(intArrayOf(Items.DREAM_VIAL_EMPTY_11151), Items.DREAM_VIAL_WATER_11152);

        companion object {
            private val itemMap = HashMap<Int, WaterVessel>()

            init {
                for (entry in values()) {
                    entry.inputs.forEach { itemMap[it] = entry }
                }
            }

            /**
             * Gets all valid empty vessel ids.
             */
            fun getValidInputs(): IntArray = itemMap.keys.toIntArray()

            /**
             * Finds a vessel type by item id.
             */
            fun fromItemId(id: Int): WaterVessel? = itemMap[id]
        }
    }
}
