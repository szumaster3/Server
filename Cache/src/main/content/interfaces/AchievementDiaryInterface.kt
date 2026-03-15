package content.interfaces

import com.alex.loaders.interfaces.ComponentType
import com.alex.tools.IfaceCopy
import content.data.Color
import content.data.InterfaceFont
import shared.consts.Components

object AchievementDiaryInterface {
    fun add() {
        IfaceCopy.to(Components.AREA_TASK_259)
            .from(Components.AREA_TASK_259)
            .startAt(31)
            .copy(31)
            .modify {
                text               = "Ardougne"
                onVarpTransmit     = arrayOf(58)
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
        val prefix = "area_task_5_"

        labels.forEachIndexed { index, label ->
            IfaceCopy.to(Components.AREA_TASK_259)
                .startAt(34 + index)
                .addComponent {
                    type               = ComponentType.TEXT
                    parentId           = 7
                    baseX              = 28
                    this.baseY         = baseY + step * index
                    baseWidth          = 152
                    baseHeight         = 15
                    fontId             = InterfaceFont.P11_FULL
                    color              = Color.RED
                    text               = label
                    shadow             = true
                    optionMask         = 2
                    rightClickOptions  = arrayOf("Read Journal")
                    onMouseHoverScript = arrayOf(45, componentHash, Color.WHITE)
                    onMouseLeaveScript = arrayOf(45, componentHash, Color.RED)
                    onVarpTransmit     = arrayOf(58)
                }
                .save()

        }
    }
}