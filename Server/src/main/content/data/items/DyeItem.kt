package content.data.items

import shared.consts.Items

enum class DyeItem(val dyeId: Int, val capeId: Int, val goblinMailId: Int, val origamiBallonId : Int) {
    BLACK(Items.BLACK_MUSHROOM_INK_4622, Items.BLACK_CAPE_1019, Items.BLACK_GOBLIN_MAIL_9055, Items.BLACK_BALLOON_9942),
    RED(Items.RED_DYE_1763, Items.RED_CAPE_1007, Items.RED_GOBLIN_MAIL_9054, Items.RED_BALLOON_9937),
    YELLOW(Items.YELLOW_DYE_1765, Items.YELLOW_CAPE_1023, Items.YELLOW_GOBLIN_MAIL_9056, Items.YELLOW_BALLOON_9935),
    BLUE(Items.BLUE_DYE_1767, Items.BLUE_CAPE_1021, Items.BLUE_GOBLIN_MAIL_287, Items.BLUE_BALLOON_9936),
    ORANGE(Items.ORANGE_DYE_1769, Items.ORANGE_CAPE_1031, Items.ORANGE_GOBLIN_MAIL_286, Items.ORANGE_BALLOON_9938),
    GREEN(Items.GREEN_DYE_1771, Items.GREEN_CAPE_1027, Items.GREEN_GOBLIN_MAIL_9057, Items.GREEN_BALLOON_9939),
    PURPLE(Items.PURPLE_DYE_1773, Items.PURPLE_CAPE_1029, Items.PURPLE_GOBLIN_MAIL_9058, Items.PURPLE_BALLOON_9940),
    PINK(Items.PINK_DYE_6955, Items.PINK_CAPE_6959, Items.PINK_GOBLIN_MAIL_9059, Items.PINK_BALLOON_9941);

    companion object {
        private val ITEM_MAP = values().associateBy { it.dyeId }
        fun forId(id: Int) = ITEM_MAP[id]
    }
}