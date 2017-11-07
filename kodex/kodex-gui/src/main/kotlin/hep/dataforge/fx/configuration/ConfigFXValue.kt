package hep.dataforge.fx.configuration

import hep.dataforge.description.ValueDescriptor
import hep.dataforge.fx.values.ValueCallback
import hep.dataforge.fx.values.ValueCallbackResponse
import hep.dataforge.fx.values.ValueChooser
import hep.dataforge.fx.values.valueChooserFactory
import hep.dataforge.meta.Configuration
import hep.dataforge.values.Value
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.ObservableStringValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.util.*

/**
 * Created by darksnake on 01-May-17.
 */
class ConfigFXValue(name: String, parent: ConfigFXNode) : ConfigFX(name, parent) {

    val descriptor: ValueDescriptor? = parent.descriptor?.valueDescriptor(name);
    override val description = descriptor?.info() ?: ""

    private val value = object : ObjectBinding<Value>() {
        init {
            bind(parent.configProperty)
        }

        override fun computeValue(): Value {
            return parent.configuration?.getValue(name, null) ?: descriptor?.defaultValue() ?: Value.NULL
        }
    }

    private val descriptorPresent = Bindings.isNotNull(descriptor)

    private val valuePresent = object : BooleanBinding() {
        init {
            bind(parentProperty, name)
        }

        override fun computeValue(): Boolean {
            return parent
                    .flatMap<Configuration> { it.config }
                    .map<Boolean> { parentConfig -> parentConfig.optValue(getName()).isPresent() }
                    .orElse(false)
        }
    }

    override fun getChildren(): ObservableList<ConfigFX> = FXCollections.emptyObservableList()

    fun setValue(value: Value) {
        parent?.getOrBuildNode().setValue(getName(), value)
        invalidate()
    }

    override fun descriptionProperty(): ObservableStringValue {
        return description
    }

    override fun descriptorPresent(): ObservableBooleanValue {
        return descriptorPresent
    }

    override fun valuePresent(): ObservableBooleanValue {
        return valuePresent
    }

    internal fun getDescriptor(): Optional<ValueDescriptor> {
        return Optional.ofNullable(descriptor.get())
    }

    fun getValue(): Value {
        return value.get()
    }

    fun valueProperty(): ObservableObjectValue<Value> {
        return value
    }


    override fun remove() {
        if (valuePresent.get()) {
            parent
                    .flatMap<Configuration> { parent -> parent.config }
                    .ifPresent { configuration -> configuration.removeValue(getName()) }
        }
        parent.ifPresent { parent -> parent.invalidate() }
    }

    fun valueChooser(): ValueChooser {
        val chooser: ValueChooser
        val callback: ValueCallback = { value: Value ->
            setValue(value)
            invalidate()
            ValueCallbackResponse(true, value, "")
        }

        if (getDescriptor().isPresent) {
            chooser = valueChooserFactory.build(getDescriptor().get(), getValue(), callback)
        } else {
            chooser = valueChooserFactory.build(getValue(), callback)
        }
        chooser.setDisplayValue(getValue())
        //        chooser.setDisabled(!protectedProperty().get());

        return chooser
    }

    override fun invalidate() {
        value.invalidate()
        valuePresent.invalidate()
        super.invalidate()
    }

}
