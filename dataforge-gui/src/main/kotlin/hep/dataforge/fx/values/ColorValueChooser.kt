package hep.dataforge.fx.values

import hep.dataforge.values.Value
import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory

/**
 * Created by darksnake on 01-May-17.
 */
class ColorValueChooser : ValueChooserBase<ColorPicker>() {
    override fun setDisplayValue(value: Value) {
        if (!value.stringValue().isEmpty()) {
            try {
                node.value = Color.valueOf(value.stringValue())
            } catch (ex: Exception) {
                LoggerFactory.getLogger(javaClass).warn("Invalid color field value: " + value.stringValue())
            }

        }
    }

    override fun buildNode(): ColorPicker {
        val node = ColorPicker()
        node.styleClass.add("split-button")
        node.maxWidth = java.lang.Double.MAX_VALUE
        node.setOnAction { _ -> value = Value.of(node.value.toString()) }
        return node
    }
}
