package content.interfaces

import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.interfaces.IComponentSettings
import com.alex.tools.Copy
import content.data.OptionMask
import shared.consts.Items
import java.util.function.Consumer

object spellbook_interface {

    fun add() {
        val copy = Copy.newInterface()
        val interfaceId = copy.targetInterface
        var parentID: Int = -1

        Copy.to(834)
            .startAt(0)
            .addComponents(Consumer { comp ->
                comp.parentId   = -1
                comp.version    = 3
                comp.type       = ComponentType.LAYER
                comp.baseX      = 0
                comp.baseY      = 0
                comp.baseWidth  = 512
                comp.baseHeight = 512
                comp.settings   = IComponentSettings(0, -1)
                parentID    = comp.componentId
            })
            .save()

        Copy.to(834)
            .startAt(1)
            .addComponents(Consumer { comp ->
                comp.name               = "blizzard_spell"
                comp.version            = 3
                comp.parentId           = parentID
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 0
                comp.baseY              = 0
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = 1707
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.COMBAT_SPELL
                comp.settings           = IComponentSettings(OptionMask.COMBAT_SPELL, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(
                    6,
                    -2147483645,
                    (parentID shl 16) or 0,
                    1708,
                    1707,
                    9,
                    "Blizzard",
                    "A low level Ice missile",
                    Items.MIST_RUNE_4695,
                    1,
                    -1,
                    0
                )
            })
            .save()

        // TODO:

        Copy.to(834)
            .startAt(2)
            .addComponents(Consumer { comp ->
                comp.name               = "invisible_spell_lv1"
                comp.version            = 3
                comp.parentId           = parentID
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 0
                comp.baseY              = 0
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = -1
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(
                    6,
                    -2147483645,
                    (parentID shl 16) or 0,
                    1710,
                    1709,
                    50,
                    "Invisible",
                    "Become invisible for 5 seconds, avoiding opponent's attacks",
                    Items.GHOSTLY_GLOVES_6110,
                    1,
                    Items.STEAM_RUNE_4694,
                    4,
                    -1,
                    0
                )
            })
            .save()

        Copy.to(834)
            .startAt(2)
            .addComponents(Consumer { comp ->
                comp.name               = "invisible_spell_lv2"
                comp.version            = 3
                comp.parentId           = parentID
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 0
                comp.baseY              = 0
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = -1
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(
                    6,
                    -2147483645,
                    (parentID shl 16) or 0,
                    -1,
                    -1,
                    70,
                    "Invisible",
                    "Become invisible for 10 seconds, avoiding opponent's attacks",
                    Items.GHOSTLY_GLOVES_6110,
                    1,
                    Items.STEAM_RUNE_4694,
                    8,
                    -1,
                    0
                )
            })
            .save()

        Copy.to(834)
            .startAt(2)
            .addComponents(Consumer { comp ->
                comp.name               = "invisible_spell_lv3"
                comp.version            = 3
                comp.parentId           = parentID
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 0
                comp.baseY              = 0
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = -1
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(
                    6,
                    -2147483645,
                    (parentID shl 16) or 0,
                    -1,
                    -1,
                    90,
                    "Invisible",
                    "Become invisible for 15 seconds, avoiding opponent's attacks", // Gain invisibility and avoid attacks for 15 seconds
                    Items.GHOSTLY_GLOVES_6110,
                    1,
                    Items.STEAM_RUNE_4694,
                    12,
                    -1,
                    0
                )
            })
            .save()

        Copy.to(834)
            .startAt(2)
            .addComponents(Consumer { comp ->
                comp.name               = "god_wars_dungeon_teleport"
                comp.version            = 3
                comp.parentId           = parentID
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 0
                comp.baseY              = 0
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = -1
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(
                    6,
                    -2147483645,
                    (parentID shl 16) or 0,
                    -1,
                    -1,
                    99,
                    "God Wars Dungeon Teleport",
                    "Teleports you to God Wars Dungeon",
                    14988,
                    1,
                    -1,
                    0
                )
            })
            .save()

        Copy.to(834)
            .startAt(2)
            .addComponents(Consumer { comp ->
                comp.name               = "guild_warehouse_teleport"
                comp.version            = 3
                comp.parentId           = parentID
                comp.type               = ComponentType.SPRITE
                comp.baseX              = 0
                comp.baseY              = 0
                comp.baseWidth          = 24
                comp.baseHeight         = 24
                comp.spriteId           = -1
                comp.optionCircumfix    = "Cast"
                comp.optionMask         = OptionMask.CAST_ON_SELF
                comp.settings           = IComponentSettings(OptionMask.CAST_ON_SELF, -1)
                comp.hasScripts         = true
                comp.onLoadScript       = arrayOf(
                    6,
                    -2147483645,
                    (parentID shl 16) or 0,
                    -1,
                    -1,
                    25,
                    "Guild Teleport",
                    "Teleport you to Guild Warehouse",
                    -1,//gcape,
                    1,
                    Items.LAW_RUNE_563,
                    1,
                    Items.AIR_RUNE_556,
                    1,
                    -1,
                    0
                )
            })
            .save()

    }
}