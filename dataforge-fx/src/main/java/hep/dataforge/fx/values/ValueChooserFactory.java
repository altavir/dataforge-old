/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.description.ValueDescriptor;
import hep.dataforge.meta.Configuration;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;

import java.util.List;

/**
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public class ValueChooserFactory {

    private ValueChooserFactory() {
    }

    public static ValueChooserFactory getInstance() {
        return ValueChooserFactoryHolder.INSTANCE;
    }

    private static class ValueChooserFactoryHolder {

        private static final ValueChooserFactory INSTANCE = new ValueChooserFactory();
    }

    private ValueChooser build(ValueDescriptor descriptor) {
        ValueChooser chooser;
        List<ValueType> types = descriptor.type();
        if (types.size() != 1) {
            chooser = new TextValueChooser();
        } else if (!descriptor.allowedValues().isEmpty()) {
            chooser = new ComboBoxValueChooser();
        } else {
            chooser = new TextValueChooser();
        }
        chooser.setDescriptor(descriptor);
        return chooser;
    }

    public ValueChooser build(ValueDescriptor descriptor, Configuration config, String path) {
        ValueChooser chooser = build(descriptor);
        if (config.hasValue(path)) {
            chooser.setValue(config.getValue(path));
        }
        chooser.setValueCallback((Value value) -> {
            config.setValue(path, value);
            return new ValueCallbackResponse(true, value, "");
        });
        return chooser;
    }
    
    public ValueChooser build(Configuration config, String path) {
        ValueChooser chooser = new TextValueChooser();
        if (config.hasValue(path)) {
            chooser.setValue(config.getValue(path));
        }
        chooser.setValueCallback((Value value) -> {
            config.setValue(path, value);
            return new ValueCallbackResponse(true, value, "");
        });
        return chooser;
    }    
}
