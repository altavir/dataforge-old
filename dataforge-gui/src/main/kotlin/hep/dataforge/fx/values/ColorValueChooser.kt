package hep.dataforge.fx.values

import hep.dataforge.values.Value
import hep.dataforge.values.asValue
import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory

/**
 * Created by darksnake on 01-May-17.
 */
class ColorValueChooser : ValueChooserBase<ColorPicker>() {
    override fun setDisplayValue(value: Value) {
        if (!value.string.isEmpty()) {
            try {
                node.value = Color.valueOf(value.string)
            } catch (ex: Exception) {
                LoggerFactory.getLogger(javaClass).warn("Invalid color field value: " + value.string)
            }

        }
    }

    override fun buildNode(): ColorPicker {
        val node = ColorPicker()
        node.styleClass.add("split-button")
        node.maxWidth = java.lang.Double.MAX_VALUE
        node.setOnAction { _ -> value = node.value.toString().asValue() }
        return node
    }
}
