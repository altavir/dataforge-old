/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values

import hep.dataforge.description.ValueDescriptor
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import org.slf4j.LoggerFactory
import tornadofx.*

/**
 * ValueChooser boilerplate
 *
 * @author Alexander Nozik
 */
abstract class ValueChooserBase<out T : Node> : ValueChooser {

    override val node by lazy { buildNode() }
    override val valueProperty = SimpleObjectProperty<Value>(Value.NULL)
    override val descriptorProperty = SimpleObjectProperty<ValueDescriptor>()


    fun resetValue() {
        setDisplayValue(currentValue())
    }

    /**
     * Current value or default value
     * @return
     */
    protected fun currentValue(): Value {
        val value = valueProperty.get()
        return if (value == null || value.type == ValueType.NULL) {
            val descriptor = descriptor
            if (descriptor != null) {
                descriptor.defaultValue()
            } else {
                Value.NULL
            }
        } else {
            value
        }
    }

    /**
     * True if builder node is successful
     *
     * @return
     */
    protected abstract fun buildNode(): T

    /**
     * Display validation error
     *
     * @param error
     */
    protected fun displayError(error: String) {
        LoggerFactory.getLogger(javaClass).error(error)
    }

    override fun setCallback(callback: ValueCallback) {
        valueProperty.onChange { newValue: Value? ->
            val response = callback(newValue ?: Value.NULL)
            if (response.value != valueProperty.get()) {
                setDisplayValue(response.value)
            }

            if (!response.success) {
                displayError(response.message)
            }
        }
    }
}
