package hep.dataforge.fx.configuration

import hep.dataforge.description.ValueDescriptor
import hep.dataforge.fx.values.ValueCallbackResponse
import hep.dataforge.fx.values.ValueChooserFactory
import hep.dataforge.values.Value
import javafx.beans.binding.ObjectBinding

/**
 * Created by darksnake on 01-May-17.
 */
class ConfigFXValue(name: String, parent: ConfigFXNode) : ConfigFX(name, parent) {

    val descriptor: ValueDescriptor? = parent.descriptor?.valueDescriptor(name);

    override fun getDescription(): String {
        return descriptor?.info() ?: ""
    }

    override val contentProperty = object : ObjectBinding<Value?>() {
        init {
            bind(parent.contentProperty)
        }

        override fun computeValue(): Value? {
            return parent.configuration?.getValue(name, null) ?: descriptor?.defaultValue()
        }
    }


    override fun getChildren(): List<ConfigFX> = ArrayList()

    fun setValue(value: Value) {
        parent?.setValue(name, value)
        invalidate()
    }

    fun getValue(): Value {
        return contentProperty.get() ?: Value.NULL
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
        contentProperty.invalidate()
    }

}
