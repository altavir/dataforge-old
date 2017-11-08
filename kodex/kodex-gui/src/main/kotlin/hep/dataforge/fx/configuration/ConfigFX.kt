package hep.dataforge.fx.configuration

import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableBooleanValue

/**
 * A node, containing relative representation of configuration node and description
 * Created by darksnake on 01-May-17.
 */
abstract class ConfigFX(val name: String, val parent: ConfigFXNode? = null) {
    abstract fun getDescription(): String
//    abstract val children: ObservableList<ConfigFX>

    //abstract val valueProperty: ObjectBinding<out Any?>;

    //abstract val valueProperty: ObservableObjectValue<Value>

    /**
     * remove itself from parent
     */
    abstract fun remove()

    abstract val isEmpty: ObservableBooleanValue

    val nameProperty = object : StringBinding() {
        override fun computeValue(): String {
            return name
        }
    }

    val descriptionProperty = object : StringBinding() {
        override fun computeValue(): String {
            return getDescription()
        }
    }


}
