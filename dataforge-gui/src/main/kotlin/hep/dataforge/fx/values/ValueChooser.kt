/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values

import hep.dataforge.description.ValueDescriptor
import hep.dataforge.values.Value
import javafx.beans.property.ObjectProperty
import javafx.scene.Node

/**
 * A value chooser object. Must have an empty constructor to be invoked by
 * reflections.
 *
 * @author [Alexander Nozik](mailto:altavir@gmail.com)
 */
interface ValueChooser {

    /**
     * Get or create a Node that could be later inserted into some parent
     * object.
     *
     * @return
     */
    val node: Node

    /**
     * The descriptor property for this value. Could be null
     *
     * @return
     */
    val descriptorProperty: ObjectProperty<ValueDescriptor?>
    var descriptor: ValueDescriptor?

    val valueProperty: ObjectProperty<Value?>
    var value: Value?



    /**
     * Set display value but do not notify listeners
     *
     * @param value
     */
    fun setDisplayValue(value: Value)


    fun setDisabled(disabled: Boolean) {
        //TODO replace by property
    }

    fun setCallback(callback:ValueCallback)
}

object ValueChooserFactory{
    private fun build(descriptor: ValueDescriptor?): ValueChooser {
        if(descriptor == null){
            return TextValueChooser();
        }
        //val types = descriptor.type
        val chooser: ValueChooser = when {
            descriptor.allowedValues.isNotEmpty() -> ComboBoxValueChooser()
            descriptor.tags.contains("widget:color") -> ColorValueChooser()
            else -> TextValueChooser()
        }
        chooser.descriptor = descriptor
        return chooser
    }

    fun build(initialValue: Value, descriptor: ValueDescriptor? = null, callback: ValueCallback): ValueChooser {
        val chooser = build(descriptor)
        if (initialValue != Value.NULL) {
            chooser.setDisplayValue(initialValue)
        }
        chooser.setCallback(callback)
        return chooser
    }
}
