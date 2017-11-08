package hep.dataforge.fx.configuration

import hep.dataforge.description.ValueDescriptor
import hep.dataforge.fx.values.ValueCallbackResponse
import hep.dataforge.fx.values.ValueChooserFactory
import hep.dataforge.values.Value
import javafx.beans.binding.ObjectBinding
import javafx.beans.value.ObservableBooleanValue
import tornadofx.*

/**
 * Created by darksnake on 01-May-17.
 */
class ConfigFXValue(name: String, parent: ConfigFXNode) : ConfigFX(name, parent) {

    val descriptor: ValueDescriptor? = parent.descriptor?.valueDescriptor(name);

    override fun getDescription(): String {
        return descriptor?.info() ?: ""
    }

    private val valueProperty = object : ObjectBinding<Value?>() {
        init {
            bind(parent.configProperty)
        }

        override fun computeValue(): Value? {
            return parent.configuration?.optValue(name)?.orElse(descriptor?.defaultValue())
        }
    }

    override val isEmpty: ObservableBooleanValue = valueProperty.booleanBinding { it == null }


    fun setValue(value: Value) {
        parent?.setValue(name, value)
        invalidate()
    }

    fun getValue(): Value {
        return valueProperty.get() ?: Value.NULL
    }

    override fun remove() {
        parent?.removeValue(name)
    }

    val valueChooser = ValueChooserFactory.build(getValue(), descriptor) { value: Value ->
        setValue(value)
        invalidate()
        ValueCallbackResponse(true, value, "")
    }

    private fun invalidate() {
        parent?.invalidate()
    }

}
