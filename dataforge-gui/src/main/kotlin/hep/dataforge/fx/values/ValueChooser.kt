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
        get() = descriptorProperty.get()
        set(desc) = descriptorProperty.set(desc)

    val valueProperty: ObjectProperty<Value?>

    var value: Value?
        get() = valueProperty.get()
        set(v) = valueProperty.set(v)


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
        val types = descriptor.type
        val chooser: ValueChooser = if (types.size != 1) {
            TextValueChooser()
        } else if (!descriptor.allowedValues.isEmpty()) {
            ComboBoxValueChooser()
        } else if (descriptor.tags.contains("color")) {
            ColorValueChooser()
        } else {
            TextValueChooser()
        }
        chooser.descriptor = descriptor
        return chooser
    }

    fun build(initialValue: Value, descriptor: ValueDescriptor? = null, callback: ValueCallback): ValueChooser {
        val chooser = build(descriptor)
        if (initialValue !== Value.NULL) {
            chooser.value = initialValue
        }
        chooser.setCallback(callback)
        return chooser
    }
}
