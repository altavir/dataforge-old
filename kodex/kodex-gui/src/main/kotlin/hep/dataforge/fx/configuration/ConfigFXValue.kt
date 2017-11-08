package hep.dataforge.fx.configuration

import hep.dataforge.description.ValueDescriptor
import hep.dataforge.fx.values.ValueCallbackResponse
import hep.dataforge.fx.values.ValueChooserFactory
import hep.dataforge.names.Name
import hep.dataforge.values.Value
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableStringValue
import tornadofx.*

/**
 * Created by darksnake on 01-May-17.
 */
class ConfigFXValue(name: String, parent: ConfigFXNode) : ConfigFX(name, parent) {

    val descriptor: ValueDescriptor? = parent.descriptor?.valueDescriptor(name);

    override val descriptionProperty: ObservableStringValue = object : StringBinding() {
        override fun computeValue(): String {
            return descriptor?.info() ?: ""
        }
    }

    private val valueProperty = object : ObjectBinding<Value?>() {
        init {
            bind(parent.configProperty)
        }

        override fun computeValue(): Value? {
            return parent.configuration?.optValue(name)?.orElse(descriptor?.defaultValue())
        }
    }

    override val isEmpty: ObservableBooleanValue = parent.configProperty.booleanBinding(valueProperty) {
        !(it?.hasValue(name) ?: false)
    }


    var value: Value
        set(value){parent?.setValue(name, value)}
        get() = valueProperty.get() ?: Value.NULL


    override fun remove() {
        parent?.removeValue(name)
    }

    override fun invalidate() {
        valueProperty.invalidate()
        valueChooser.setDisplayValue(value)
    }

    override fun invalidateValue(path: Name) {
        if(path.isEmpty){
            invalidate()
        }
    }

    override fun invalidateNode(path: Name) {
        //do nothing
    }

    val valueChooser = ValueChooserFactory.build(value, descriptor) { value: Value ->
        this.value = value
        ValueCallbackResponse(true, value, "")
    }


}
