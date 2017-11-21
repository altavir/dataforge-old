package hep.dataforge.meta;

import hep.dataforge.description.Described;
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;

import java.util.Optional;

/**
 * A metoid object with descriptor. Values from descriptor automatically substitute missing values from meta.
 */
public interface DescribedMetoid extends Metoid, ValueProvider, Described {

    /**
     * If this object's meta provides given value, return it, otherwise, use
     * descriptor
     *
     * @param name
     * @return
     */
    @Override
    default Optional<Value> optValue(String name) {
        return Optionals.either(getMeta().optValue(name)).or(() -> {
            if (getDescriptor().hasDefaultForValue(name)) {
                return Optional.of(getDescriptor().valueDescriptor(name).defaultValue());
            } else {
                return Optional.empty();
            }
        }).opt();
    }

    /**
     * true if this object's meta
     *
     * @param name
     * @return
     */
    @Override
    default boolean hasValue(String name) {
        return getMeta().hasValue(name) || getDescriptor().hasDefaultForValue(name);
    }
}
