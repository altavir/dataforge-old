package hep.dataforge.control.devices;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
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
    String METASTATE_TARGET = "metastate";

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

    Meta getMetaState(String name);

    @Provides(STATE_TARGET)
    default Optional<Value> optState(String stateName) {
        if (!hasState(stateName)) {
            return Optional.empty();
        } else {
            Value state = getState(stateName);
            if (state == null || state.isNull()) {
                return Optional.empty();
            } else {
                return Optional.of(state);
            }
        }
    }

    @Provides(METASTATE_TARGET)
    default Optional<Meta> optMetaState(String stateName) {
        if (!hasMetaState(stateName)) {
            return Optional.empty();
        } else {
            Meta state = getMetaState(stateName);
            if (state == null || state.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(state);
            }
        }
    }

    @ProvidesNames(STATE_TARGET)
    default Stream<String> listStates() {
        return getStateDefs().stream().map(it -> it.value().name());
    }

    @ProvidesNames(METASTATE_TARGET)
    default Stream<String> listMetaStates() {
        return getMetaStateDefs().stream().map(it -> it.value().name());
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

    void setMetaState(String name, Meta value);

    /**
     * A list of all available states
     *
     * @return
     */
    default List<StateDef> getStateDefs() {
        return DescriptorUtils.listAnnotations(this.getClass(), StateDef.class, true);
    }

    /**
     * Find if current device has defined state with given name
     *
     * @param stateName
     * @return
     */
    default boolean hasState(String stateName) {
        return getStateDefs().stream().anyMatch(stateDef -> stateDef.value().name().equals(stateName));
    }

    /**
     * Find a state definition for given name. Null if not found.
     *
     * @param name
     * @return
     */
    default Optional<StateDef> optStateDef(String name) {
        return getStateDefs().stream().filter((stateDef) -> stateDef.value().name().equals(name)).findFirst();
    }


    /**
     * A list of all available metastates
     *
     * @return
     */
    default List<MetaStateDef> getMetaStateDefs() {
        return DescriptorUtils.listAnnotations(this.getClass(), MetaStateDef.class, true);
    }

    /**
     * Find if current device has defined metastate with given name
     *
     * @param stateName
     * @return
     */
    default boolean hasMetaState(String stateName) {
        return getMetaStateDefs().stream().anyMatch(stateDef -> stateDef.value().name().equals(stateName));
    }

    /**
     * Find a state definition for given name. Null if not found.
     *
     * @param name
     * @return
     */
    default Optional<MetaStateDef> optMetaStateDef(String name) {
        return getMetaStateDefs().stream().filter((stateDef) -> stateDef.value().name().equals(name)).findFirst();
    }

    /**
     * Get the descriptor for a given state
     * @param stateName
     * @return
     */
    default Optional<ValueDef> getStateDescriptor(String stateName){
        return optStateDef(stateName).map(StateDef::value);
    }

    /**
     * Get descriptor for a meta state. Could be overriden for a custom descriptor
     * @param stateName
     * @return
     */
    default Optional<NodeDef> getMetaStateDescriptor(String stateName){
        return optMetaStateDef(stateName).map(MetaStateDef::value);
    }
}
