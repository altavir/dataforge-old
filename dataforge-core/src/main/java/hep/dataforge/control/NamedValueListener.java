package hep.dataforge.control;

import hep.dataforge.values.Value;

/**
 * Created by darksnake on 25-May-17.
 */
public interface NamedValueListener {
    void pushValue(String valueName, Value value);

    default void pushValue(String valueName, Object obj) {
        pushValue(valueName, Value.of(obj));
    }
}
