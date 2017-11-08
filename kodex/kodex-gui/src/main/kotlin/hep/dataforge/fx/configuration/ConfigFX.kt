package hep.dataforge.fx.configuration

import hep.dataforge.names.Name
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableStringValue

/**
 * A node, containing relative representation of configuration node and description
 * Created by darksnake on 01-May-17.
 */
abstract class ConfigFX(val name: String, val parent: ConfigFXNode? = null) {
    /**
     * remove itself from parent
     */
    abstract fun remove()

    abstract val isEmpty: ObservableBooleanValue
    abstract val descriptionProperty: ObservableStringValue

    val nameProperty = object : StringBinding() {
        override fun computeValue(): String {
            return name
        }
    }


    abstract fun invalidate()

    abstract fun invalidateValue(path: Name)

    abstract fun invalidateNode(path: Name)
}
