/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration;

import com.sun.javafx.binding.ExpressionHelper;
import hep.dataforge.meta.ConfigChangeListener;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Configuration value represented as JavaFX property. Using slightly modified
 * JavaFx ObjectPropertyBase code.
 *
 * @author Alexander Nozik
 */
public abstract class ConfigValuePropertyBase<T> extends ObjectProperty<T> {
    //TODO add descriptor validation

    private final Configuration config;
    private final String valueName;

    private boolean valid = false;
    private ExpressionHelper<T> helper = null;
    private final ConfigValueListener cfgListener = new ConfigValueListener();
    /**
     * current value cached to avoid call of configuration parsing
     */
    private T value;

    public ConfigValuePropertyBase(Configuration config, String valueName, T defaultValue) {
        this(config, valueName);
        this.value = defaultValue;
    }

    public ConfigValuePropertyBase(Configuration config, String valueName) {
        this.config = config;
        this.valueName = valueName;
        //adding a weak observer to configuration
        config.addObserver(cfgListener, false);
    }

    @Override
    public Configuration getBean() {
        return config;
    }

    @Override
    public String getName() {
        return valueName;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Sends notifications to all attached
     * {@link javafx.beans.InvalidationListener InvalidationListeners} and
     * {@link javafx.beans.value.ChangeListener ChangeListeners}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set} or in case of a bound property, if the binding
     * becomes invalid.
     */
    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(helper);
    }

    private void markInvalid() {
        if (valid) {
            valid = false;
            fireValueChangedEvent();
        }
    }

    protected abstract T getConfigValue(Configuration cfg, String valueName);

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized T get() {
        if (!valid) {
            valid = true;
            if (config.hasValue(valueName) || value == null) {
                value = getConfigValue(config, valueName);
            }
        }
        return value;
    }

    protected void setConfigValue(Configuration cfg, String valueName, T newValue) {
        config.setValue(valueName, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(T newValue) {
        setConfigValue(config, valueName, newValue);
        //invalidation not required since it obtained automatically via listener
        //markInvalid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBound() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(final ObservableValue<? extends T> newObservable) {
        throw new RuntimeException("Configuration property could not be bound");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbind() {
        throw new RuntimeException("Configuration property could not be unbound");
    }

    /**
     * Returns a string representation of this {@code ObjectPropertyBase}
     * object.
     *
     * @return a string representation of this {@code ObjectPropertyBase}
     * object.
     */
    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("ConfigurationValueProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && (!name.equals(""))) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get());
        result.append("]");
        return result.toString();
    }

    private final class ConfigValueListener implements ConfigChangeListener {

        @Override
        public void notifyValueChanged(String name, Value oldItem, Value newItem) {
            if (valueName.equals(name) && !oldItem.equals(newItem)) {
                markInvalid();
            }
        }

        @Override
        public void notifyElementChanged(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem) {
            //do nothing
        }
    }

}
