package content.interfaces

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.interfaces.IComponentSettings
import com.alex.tools.IfaceCopy
import content.data.OptionMask
import content.data.Sprite
import shared.consts.Items
import java.util.function.Consumer


// TODO.
object CustomSpellBookInterface {

    fun add() {

        val tabLayer = ComponentDefinition.getInterfaceComponent(834,16)?.componentHash

        IfaceCopy.to(834)
            .startAt(0)
            .addComponents(Consumer { comp ->
                comp.name               = "guild_home_teleport"
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
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, 356, 406, 0, "Guild Hall Home Teleport", "Requires no runes - recharge time 30 mins. Warning: This spell takes a long time to cast and will be interrupted by combat.", -1, 0, -1, 0, -1, 0, -1, 0)
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
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, 356, 406, 0, Sprite.BLIZZARD_SPELL_ON, Sprite.BLIZZARD_SPELL_OFF, 9, "Blizzard", "A low level Ice missile", Items.MIST_RUNE_4695, 1, -1, 0, -1, 0, -1, 0)
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
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, Sprite.INVISIBLE_SPELL_ON, Sprite.INVISIBLE_SPELL_OFF, 50, "Lvl-1 Invisible", "Become invisible for 5 seconds, avoiding opponent's attacks", Items.GHOSTLY_GLOVES_6110, 1, Items.STEAM_RUNE_4694, 4, -1, 0, -1, 0)
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
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, Sprite.INVISIBLE_SPELL_LV2_ON, Sprite.INVISIBLE_SPELL_LV2_OFF, 70, "Lvl-2 Invisible", "Become invisible for 10 seconds, avoiding opponent's attacks", Items.GHOSTLY_GLOVES_6110, 1, Items.STEAM_RUNE_4694, 8, -1, 0, -1, 0)
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
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, Sprite.INVISIBLE_SPELL_LV3_ON, Sprite.INVISIBLE_SPELL_LV3_OFF, 90, "Lvl-3 Invisible", "Become invisible for 15 seconds, avoiding opponent's attacks", Items.GHOSTLY_GLOVES_6110, 1, Items.STEAM_RUNE_4694, 12, -1, 0, -1, 0)
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
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, Sprite.GWD_TELEPORT_SPELL_ON, Sprite.GWD_TELEPORT_SPELL_OFF, 99, "God Wars Dungeon Teleport", "Teleports you to God Wars Dungeon", 0, 1, 0, 1, -1, 0, -1, 0)
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
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, Sprite.MISSION_TELEPORT_SPELL_ON, Sprite.MISSION_TELEPORT_SPELL_OFF, 25, "Guild Teleport", "Teleport you to Guild Warehouse", 0, 1, Items.LAW_RUNE_563, 1, Items.AIR_RUNE_556, 1, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(7)
            .addComponents(Consumer { comp ->
                comp.name               = "create_food_spell"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 151
                comp.baseY              = 36
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = Sprite.CREATE_FOOD_SPELL_OFF
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, Sprite.CREATE_FOOD_SPELL_ON, Sprite.CREATE_FOOD_SPELL_OFF, 80, "Create Meat", "Creates random meat. Reduces Magic Level by 3.", Items.LAVA_RUNE_4699, 2, Items.NATURE_RUNE_561, 1, 0, -1, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(8)
            .addComponents(Consumer { comp ->
                comp.name               = "sort_button_highlight"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 43
                comp.baseY              = 5
                comp.baseWidth          = 20
                comp.baseHeight         = 20
                comp.xMode              = 2
                comp.yMode              = 2
                comp.spriteId           = 1701
                comp.optionMask         = 1026
                comp.settings           = IComponentSettings(1026, -1)
                comp.hasScripts         = true
                comp.varpTriggers       = intArrayOf(1376)// VarbitID: 357
                comp.rightClickOptions  = arrayOf("Sort", "", "", "", "", "", "", "", "", "  ")
                comp.onMouseLeaveScript = arrayOf(2060, -2147483645)
                comp.onVarpTransmit     = arrayOf(2057)
                comp.onMouseRepeat      = arrayOf(2061, -2147483645)
                comp.onClickRepeat      = arrayOf(2058, 0)
            }).save()

        IfaceCopy.to(834)
            .startAt(9)
            .addComponents(Consumer { comp ->
                comp.name               = "sort_button_v1"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 24
                comp.baseY              = 5
                comp.baseWidth          = 20
                comp.baseHeight         = 20
                comp.xMode              = 2
                comp.yMode              = 2
                comp.spriteId           = 1702
                comp.optionMask         = 1026
                comp.settings           = IComponentSettings(1026, -1)
                comp.hasScripts         = true
                comp.rightClickOptions  = arrayOf("Sort", "", "", "", "", "", "", "", "", "   ")
                comp.onMouseLeaveScript = arrayOf(2060, -2147483645)
                comp.onMouseRepeat      = arrayOf(2061, -2147483645)
                comp.onClickRepeat      = arrayOf(2058, 1)
            }).save()

        IfaceCopy.to(834)
            .startAt(10)
            .addComponents(Consumer { comp ->
                comp.name               = "sort_button_v2"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 5
                comp.baseY              = 5
                comp.baseWidth          = 20
                comp.baseHeight         = 20
                comp.xMode              = 2
                comp.yMode              = 2
                comp.spriteId           = 1702
                comp.optionMask         = 1026
                comp.settings           = IComponentSettings(1026, -1)
                comp.hasScripts         = true
                comp.rightClickOptions  = arrayOf("Sort", "", "", "", "", "", "", "", "", "   ")
                comp.onMouseLeaveScript = arrayOf(2060, -2147483645)
                comp.onMouseRepeat      = arrayOf(2061, -2147483645)
                comp.onClickRepeat      = arrayOf(2058, 2)
            }).save()

        IfaceCopy.to(834)
            .startAt(11)
            .addComponents(Consumer { comp ->
                comp.name               = "sort_sprite_v1"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 43
                comp.baseY              = 5
                comp.baseWidth          = 20
                comp.baseHeight         = 20
                comp.xMode              = 2
                comp.yMode              = 2
                comp.spriteId           = 1704
                comp.optionMask         = 1024
                comp.settings           = IComponentSettings(1024, -1)
                comp.rightClickOptions  = arrayOf("", "", "", "", "", "", "", "", "", "   ")
            }).save()

        IfaceCopy.to(834)
            .startAt(12)
            .addComponents(Consumer { comp ->
                comp.name               = "sort_sprite_v2"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 24
                comp.baseY              = 5
                comp.baseWidth          = 20
                comp.baseHeight         = 20
                comp.xMode              = 2
                comp.yMode              = 2
                comp.spriteId           = 1705
                comp.optionMask         = 1024
                comp.settings           = IComponentSettings(1024, -1)
                comp.rightClickOptions  = arrayOf("", "", "", "", "", "", "", "", "", "   ")
            }).save()

        IfaceCopy.to(834)
            .startAt(13)
            .addComponents(Consumer { comp ->
                comp.name               = "sort_sprite_v3"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 5
                comp.baseY              = 5
                comp.baseWidth          = 20
                comp.baseHeight         = 20
                comp.xMode              = 2
                comp.yMode              = 2
                comp.spriteId           = 1706
                comp.optionMask         = 1024
                comp.settings           = IComponentSettings(1024, -1)
                comp.rightClickOptions  = arrayOf("", "", "", "", "", "", "", "", "", "   ")
            }).save()

        IfaceCopy.to(834)
            .startAt(14)
            .addComponents(Consumer { comp ->
                comp.name               = "layer_v1"
                comp.version            = 3
                comp.type               = ComponentType.LAYER
                comp.baseY              = 29
                comp.baseWidth          = 10
                comp.baseHeight         = 20
                comp.xMode              = 2
                comp.yMode              = 1
                comp.widthMode          = 1
                comp.optionMask         = 1024
                comp.settings           = IComponentSettings(1024, -1)
                comp.rightClickOptions  = arrayOf("", "", "", "", "", "", "", "", "", "  ")
            }).save()

        val parentLayer = ComponentDefinition.getInterfaceComponent(834, 14)!!.componentHash

        IfaceCopy.to(834)
            .startAt(15)
            .addComponents(Consumer { comp ->
                comp.name               = "layer_v2"
                comp.version            = 3
                comp.parentId           = parentLayer
                comp.type               = ComponentType.LAYER
                comp.baseX              = 3
                comp.baseY              = 2
                comp.baseWidth          = 42
                comp.baseHeight         = 14
                comp.optionMask         = 1024
                comp.settings           = IComponentSettings(1024, -1)
                comp.rightClickOptions  = arrayOf("", "", "", "", "", "", "", "", "", "  ")
            }).save()

        IfaceCopy.to(834)
            .startAt(16)
            .addComponents(Consumer { comp ->
                comp.name               = "tab_border"
                comp.version            = 3
                comp.parentId           = -1
                comp.type               = ComponentType.LAYER
                comp.baseX              = 0
                comp.baseY              = 0
                comp.baseWidth          = 190
                comp.baseHeight         = 261
                comp.settings           = IComponentSettings(-1, -1)
            }).save()




        IfaceCopy.to(834)
            .startAt(999999999)
            .addComponents(Consumer { comp ->
                comp.version            = 3
                comp.type               = 5
                comp.baseX              = 19
                comp.baseY              = 92
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = 999999999
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_GROUND
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_GROUND, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(6, -2147483645, tabLayer, -1, -1, 64, "Arctic Dig", "Allows you to dig up treasure in the snow without a spade", Items.ELEMENTAL_RUNE_12850, 4, Items.DUST_RUNE_4696, 3, -1, 0, -1, 0)
            }).save()

    }
}