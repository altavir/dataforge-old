/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.Named;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.SimpleMetaMorph;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A descriptor for meta value
 *
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
public class ValueDescriptor extends SimpleMetaMorph implements Named {

    public static ValueDescriptor build(ValueDef val) {
        MetaBuilder builder = new MetaBuilder("value")
                .setValue("name", val.name())
                .setValue("type", val.type())
                .setValue("tags", val.tags());

        if (!val.required()) {
            builder.setValue("required", val.required());
        }

        if (!val.multiple()) {
            builder.setValue("multiple", val.multiple());
        }

        if (!val.info().isEmpty()) {
            builder.setValue("info", val.info());
        }

        if (val.allowed().length > 0) {
            builder.setValue("allowedValues", val.allowed());
        } else if(val.enumeration() != Object.class){
            if(val.enumeration().isEnum()) {
                Object[] values = val.enumeration().getEnumConstants();
                builder.setValue("allowedValues", Stream.of(values).map(Object::toString));
            } else {
                throw new RuntimeException("Only enumeration classes are allowed in 'enumeration' annotation property");
            }
        }



        if (!val.def().isEmpty()) {
            builder.setValue("default", val.def());
        }
        return new ValueDescriptor(builder);
    }

    public static ValueDescriptor empty(String valueName){
        MetaBuilder builder = new MetaBuilder("value")
                .setValue("name", valueName);
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
        return getMeta().getString("info", "");
    }

    /**
     * True if multiple values with this name are allowed.
     *
     * @return
     */
    public boolean isMultiple() {
        return getMeta().getBoolean("multiple", true);
    }

    /**
     * True if the value is required
     *
     * @return
     */
    public boolean isRequired() {
        return getMeta().getBoolean("required", false);
    }

    /**
     * Value name
     *
     * @return
     */
    @NotNull
    @Override
    public String getName() {
        return getMeta().getString("name", "");
    }

    /**
     * A list of allowed ValueTypes. Empty if any value type allowed
     *
     * @return
     */
    public List<ValueType> type() {
        if (getMeta().hasValue("type")) {
            return getMeta().getValue("type").listValue()
                    .stream()
                    .map((v) -> ValueType.valueOf(v.stringValue()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<String> tags() {
        if (getMeta().hasValue("tags")) {
            return Arrays.asList(getMeta().getStringArray("tags"));
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
        return (type().isEmpty() || type().contains(ValueType.STRING) || type().contains(value.getType()))
                && (allowedValues().isEmpty() || allowedValues().containsKey(value));
    }

    /**
     * Check if there is default for this value
     *
     * @return
     */
    public boolean hasDefault() {
        return getMeta().hasValue("default");
    }

    /**
     * The default for this value. Null if there is no default.
     *
     * @return
     */
    public Value defaultValue() {
        return getMeta().getValue("default", Value.NULL);
    }

    /**
     * A list of allowed values with descriptions. If empty than any value is
     * allowed.
     *
     * @return
     */
    public Map<Value, String> allowedValues() {
        Map<Value, String> map = new HashMap<>();
        if (getMeta().hasMeta("allowedValue")) {
            for (Meta allowed : getMeta().getMetaList("allowedValue")) {
                map.put(allowed.getValue("value"), allowed.getString("description", ""));
            }
        } else if (getMeta().hasValue("allowedValues")) {
            for (Value val : getMeta().getValue("allowedValues").listValue()) {
                map.put(val, "");
            }
        } else if (type().size() == 1 && type().get(0) == ValueType.BOOLEAN) {
            map.put(Value.of(true), "");
            map.put(Value.of(false), "");
        }

        return map;
    }

}
