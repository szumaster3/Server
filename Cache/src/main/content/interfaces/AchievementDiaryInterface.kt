package content.interfaces

import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.interfaces.IComponentSettings
import com.alex.tools.IfaceCopy
import shared.consts.Components
import java.util.function.Consumer

object AchievementDiaryInterface {

    fun add() {
        // Modify component 31.
        IfaceCopy.to(Components.AREA_TASK_259)
            .from(Components.AREA_TASK_259)
            .startAt(31)
            .copy(31)
            .modify { comp ->
                comp.parentId           = 16973831
                comp.text               = "Ardougne"
                comp.shadow             = true
                comp.optionMask         = 2
                comp.settings           = IComponentSettings(2, -1)
                comp.rightClickOptions  = arrayOf("Read Journal", "", "", "", "", "", "", "", "", " ")
                comp.onMouseHoverScript = arrayOf(45, comp.componentHash, 16777215)
                comp.onMouseLeaveScript = arrayOf(45, comp.componentHash, 16711680)
                comp.onVarpTransmit     = arrayOf(58)
            }
            .save()

        // Magic of area task interface:
        // Component 7 is the layer for all parent components and use script 1393 on load.
        // The script calculates the vertical position for component 31 using internal transformations
        // and offsets, then positions it inside the scroll layer.
        //
        // Based on this calculated position, the script also adjusts the scroll height of component 7
        // (baseY + 95) and initializes the scrollbar by linking component 10 to component 7.
        //
        // Since we currently cannot use the script without editing it this,
        // we replicate the layout logic by using a fixed baseY value so that additional
        // components align correctly within the scrollable task list.

        val baseY = 455
        val step = 14
        val labels = listOf("Easy", "Medium", "Hard")

        labels.forEachIndexed { index, text ->
            IfaceCopy.to(Components.AREA_TASK_259)
                .startAt(34 + index)
                .addComponents(Consumer { comp ->
                    comp.version            = 3
                    comp.parentId           = 16973831
                    comp.type               = ComponentType.TEXT
                    comp.baseX              = 28
                    comp.baseY              = baseY + step * index
                    comp.baseWidth          = 152
                    comp.baseHeight         = 15
                    comp.fontId             = 494
                    comp.color              = 16711680
                    comp.text               = text
                    comp.shadow             = true
                    comp.optionMask         = 2
                    comp.settings           = IComponentSettings(2, -1)
                    comp.rightClickOptions  = arrayOf("Read Journal", "", "", "", "", "", "", "", "", " ")
                    comp.onMouseHoverScript = arrayOf(45, comp.componentHash, 16777215)
                    comp.onMouseLeaveScript = arrayOf(45, comp.componentHash, 16711680)
                    comp.onVarpTransmit     = arrayOf(58)
                }).save()
        }
    }
}