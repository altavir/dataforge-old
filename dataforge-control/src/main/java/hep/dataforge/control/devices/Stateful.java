package hep.dataforge.control.devices;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.providers.Provider;
import hep.dataforge.providers.Provides;
import hep.dataforge.providers.ProvidesNames;
import hep.dataforge.values.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * An object that could have a set of readonly or read/write states
 */
public interface Stateful extends Provider {

    String STATE_TARGET = "state";

    /**
     * Get the state with given name. Null if such state not found or
     * undefined. This operation is synchronous so use it with care. In general,
     * it is recommended to use asynchronous state change listeners instead of
     * this method.
     *
     * @param name
     * @return
     */
    Value getState(String name);

    @Provides(STATE_TARGET)
    default Optional<Value> optState(String stateName) {
        if (!hasState(stateName)) {
            return Optional.empty();
        } else {
            Value state = getState(stateName);
            if (state.isNull()) {
                return Optional.empty();
            } else {
                return Optional.of(state);
            }
        }
    }

    @ProvidesNames(STATE_TARGET)
    default Stream<String> listStates() {
        return stateDefs().stream().map(it -> it.value().name());
    }

    default Optional<Boolean> optBooleanState(String name) {
        return optState(name).map(Value::booleanValue);
    }

    /**
     * Request asynchronous state change for state with given name and return the state value after change
     *
     * @param name
     * @param value
     * @return the actual state after set
     */
    void setState(String name, Object value);

    /**
     * A list of all available states
     *
     * @return
     */
    default List<StateDef> stateDefs() {
        return DescriptorUtils.listAnnotations(this.getClass(), StateDef.class, true);
    }

    /**
     * Find if current device has defined state with given name
     *
     * @param stateName
     * @return
     */
    default boolean hasState(String stateName) {
        return stateDefs().stream().anyMatch(stateDef -> stateDef.value().name().equals(stateName));
    }

    /**
     * Find a state definition for given name. Null if not found.
     *
     * @param name
     * @return
     */
    default Optional<StateDef> optStateDef(String name) {
        return stateDefs().stream().filter((stateDef) -> stateDef.value().name().equals(name)).findFirst();
    }
}
