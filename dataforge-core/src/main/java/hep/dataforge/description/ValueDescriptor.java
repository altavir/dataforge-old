/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.content.AnonimousNotAlowed;
import hep.dataforge.content.Named;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A descriptor for meta value
 *
 * @author Alexander Nozik
 */
@AnonimousNotAlowed
public class ValueDescriptor extends DescriptorBase implements Named {

    public static ValueDescriptor build(ValueDef val) {
        MetaBuilder builder = new MetaBuilder("value")
                .setValue("name", val.name())
                .setValue("type", val.type());

        if (!val.required()) {
            builder.setValue("required", val.required());
        }
        
        if (!val.multiple()) {
            builder.setValue("multiple", val.multiple());
        }        

        if (!val.info().isEmpty()) {
            builder.setValue("info", val.info());
        }

        if (!val.allowed().isEmpty()) {
            builder.setValue("allowedValues", val.allowed());
        }

        if (!val.def().isEmpty()) {
            builder.setValue("default", val.def());
        }
        return new ValueDescriptor(builder);
    }

    public ValueDescriptor(Meta meta) {
        super(meta);
    }

    /**
     * The value info
     *
     * @return
     */
    public String info() {
        return meta().getString("info", "");
    }

    /**
     * True if multiple values with this name are allowed.
     *
     * @return
     */
    public boolean isMultiple() {
        return meta().getBoolean("multiple", true);
    }

    /**
     * True if the value is required
     *
     * @return
     */
    public boolean isRequired() {
        return meta().getBoolean("required", false);
    }

    /**
     * Value name
     *
     * @return
     */
    @Override
    public String getName() {
        return meta().getString("name", "");
    }

    /**
     * A list of allowed ValueTypes. Empty if any value type allowed
     *
     * @return
     */
    public List<ValueType> type() {
        if (meta().hasValue("type")) {
            return meta().getValue("type").listValue()
                    .stream()
                    .<ValueType>map((v) -> ValueType.valueOf(v.stringValue()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Check if given value is allowed for here. The type should be allowed and
     * if it is value should be within allowed values
     *
     * @param value
     * @return
     */
    public boolean isValueAllowed(Value value) {
        return (type().isEmpty() || type().contains(value.valueType()))
                && (allowedValues().isEmpty() || allowedValues().containsKey(value));
    }

    /**
     * Check if there is default for this value
     *
     * @return
     */
    public boolean hasDefault() {
        return meta().hasValue("default");
    }

    /**
     * The default for this value. Null if there is no default.
     *
     * @return
     */
    public Value defaultValue() {
        return meta().getValue("default", null);
    }

    /**
     * A list of allowed values with descriptions. If empty than any value is
     * allowed.
     *
     * @return
     */
    public Map<Value, String> allowedValues() {
        Map<Value, String> map = new HashMap<>();
        if (meta().hasNode("allowedValue")) {
            for (Meta allowed : meta().getNodes("allowedValue")) {
                map.put(allowed.getValue("value"), allowed.getString("description", ""));
            }
        }
        if (meta().hasValue("allowedValues")) {
            for (Value val : meta().getValue("allowedValues").listValue()) {
                map.put(val, "");
            }
        }

        return map;
    }
}
