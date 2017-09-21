package hep.dataforge.data;

import hep.dataforge.exceptions.AutoCastException;

/**
 * The object that could be automatically transformed to another type
 */
public interface AutoCastable {
    default <T> T asType(Class<T> type) {
        if (type.isAssignableFrom(getClass())) {
            return type.cast(this);
        } else {
            throw new AutoCastException(getClass(), type);
        }
    }
}
