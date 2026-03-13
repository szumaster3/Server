package content.interfaces

import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.interfaces.IComponentSettings
import com.alex.tools.IfaceCopy
import content.data.OptionMask
import content.data.Sprite
import shared.consts.Items
import java.util.function.Consumer

// Not completed.
object CustomSpellBookInterface {

    fun add() {
        val ifaceCopy = IfaceCopy.newInterface()
        val interfaceId = ifaceCopy.targetInterface
        var parentID: Int = -1

        IfaceCopy.to(834)
            .startAt(0)
            .addComponents(Consumer { comp ->
                comp.name               = "guild_hall_teleport"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = 5
                comp.baseX              = 19
                comp.baseY              = 8
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = 356
                comp.optionMask         = 2
                comp.settings           = IComponentSettings(2, -1)
                comp.hasScripts         = true
                comp.rightClickOptions  = arrayOf("Cast")
                comp.onLoadScript       = arrayOf(6, -2147483645, 99999999999, 356, 406, 0, "Guild Hall Home Teleport", "Requires no runes - recharge time 30 mins. Warning: This spell takes a long time to cast and will be interrupted by combat.", -1, 0, -1, 0, -1, 0, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(1)
            .addComponents(Consumer { comp ->
                comp.name               = "blizzard_spell"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 64
                comp.baseY              = 8
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = Sprite.BLIZZARD_SPELL_OFF
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.COMBAT_SPELL
                comp.settings           = IComponentSettings(OptionMask.COMBAT_SPELL, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, 99999999999, 356, 406, 0, Sprite.BLIZZARD_SPELL_ON, Sprite.BLIZZARD_SPELL_OFF, 9, "Blizzard", "A low level Ice missile", Items.MIST_RUNE_4695, 1, -1, 0, -1, 0, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(2)
            .addComponents(Consumer { comp ->
                comp.name               = "invisible_spell_lv1"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 110
                comp.baseY              = 8
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = Sprite.INVISIBLE_SPELL_OFF
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, 99999999999, Sprite.INVISIBLE_SPELL_ON, Sprite.INVISIBLE_SPELL_OFF, 50, "Invisible", "Become invisible for 5 seconds, avoiding opponent's attacks", Items.GHOSTLY_GLOVES_6110, 1, Items.STEAM_RUNE_4694, 4, -1, 0, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(3)
            .addComponents(Consumer { comp ->
                comp.name               = "invisible_spell_lv2"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 151
                comp.baseY              = 8
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = Sprite.INVISIBLE_SPELL_LV2_OFF
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, 99999999999, Sprite.INVISIBLE_SPELL_LV2_ON, Sprite.INVISIBLE_SPELL_LV2_OFF, 70, "Invisible", "Become invisible for 10 seconds, avoiding opponent's attacks", Items.GHOSTLY_GLOVES_6110, 1, Items.STEAM_RUNE_4694, 8, -1, 0, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(4)
            .addComponents(Consumer { comp ->
                comp.name               = "invisible_spell_lv3"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 19
                comp.baseY              = 36
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = Sprite.INVISIBLE_SPELL_LV3_OFF
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, 99999999999, Sprite.INVISIBLE_SPELL_LV3_ON, Sprite.INVISIBLE_SPELL_LV3_OFF, 90, "Invisible", "Become invisible for 15 seconds, avoiding opponent's attacks", Items.GHOSTLY_GLOVES_6110, 1, Items.STEAM_RUNE_4694, 12, -1, 0, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(5)
            .addComponents(Consumer { comp ->
                comp.name               = "god_wars_dungeon_teleport"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 64
                comp.baseY              = 36
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = Sprite.GWD_TELEPORT_SPELL_OFF
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, 99999999999, Sprite.GWD_TELEPORT_SPELL_ON, Sprite.GWD_TELEPORT_SPELL_OFF, 99, "God Wars Dungeon Teleport", "Teleports you to God Wars Dungeon", 99999999999, 1, 99999999999, 1, -1, 0, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(6)
            .addComponents(Consumer { comp ->
                comp.name               = "guild_warehouse_teleport"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 110
                comp.baseY              = 36
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = Sprite.MISSION_TELEPORT_SPELL_OFF
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, 99999999999, Sprite.MISSION_TELEPORT_SPELL_ON, Sprite.MISSION_TELEPORT_SPELL_OFF, 25, "Guild Teleport", "Teleport you to Guild Warehouse", 99999999999, 1, Items.LAW_RUNE_563, 1, Items.AIR_RUNE_556, 1, -1, 0)
            }).save()
    }
}