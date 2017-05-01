package hep.dataforge.fx.configuration;

import hep.dataforge.description.ValueDescriptor;
import hep.dataforge.fx.values.ValueCallback;
import hep.dataforge.fx.values.ValueCallbackResponse;
import hep.dataforge.fx.values.ValueChooser;
import hep.dataforge.fx.values.ValueChooserFactory;
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Optional;

/**
 * Created by darksnake on 01-May-17.
 */
public class ConfigFXValue extends ConfigFX {

    public static ConfigFXValue build(ConfigFXNode parent, String name) {
        ConfigFXValue res = new ConfigFXValue();
        res.nameProperty().setValue(name);
        res.parentProperty.setValue(parent);
        return res;
    }

    private ObservableObjectValue<ValueDescriptor> descriptor = new ObjectBinding<ValueDescriptor>() {
        {
            bind(parentProperty, name);
        }

        @Override
        protected ValueDescriptor computeValue() {
            return getParent()
                    .flatMap(ConfigFXNode::getDescriptor)
                    .map(descriptor -> descriptor.valueDescriptor(getName()))
                    .orElse(null);
        }
    };

    private ObservableStringValue description = new StringBinding() {
        {
            bind(descriptor);
        }

        @Override
        protected String computeValue() {
            ValueDescriptor d = descriptor.get();
            return d == null ? "" : d.info();
        }
    };

    private ObjectBinding<Value> value = new ObjectBinding<Value>() {
        {
            bind(parentProperty, name, descriptor);
        }

        @Override
        protected Value computeValue() {
            return Optionals.either(
                    getParent()
                            .flatMap(ConfigFXNode::getConfig)
                            .flatMap(parentConfig -> parentConfig.optValue(getName()))
            ).or(
                    Optional.ofNullable(descriptor.get())
                            .map(ValueDescriptor::defaultValue)
            ).opt().orElse(Value.NULL);
        }
    };

    private ObservableBooleanValue descriptorPresent = Bindings.isNotNull(descriptor);

    private BooleanBinding valuePresent = new BooleanBinding() {
        {
            bind(parentProperty, name);
        }

        @Override
        protected boolean computeValue() {
            return getParent()
                    .flatMap(ConfigFXNode::getConfig)
                    .map(parentConfig -> parentConfig.optValue(getName()).isPresent())
                    .orElse(false);
        }
    };


    private ConfigFXValue() {
    }

    public void setValue(Value value) {
        parentProperty.get().getOrBuildNode().setValue(getName(), value);
        invalidate();
    }

    @Override
    public ObservableStringValue descriptionProperty() {
        return description;
    }

    @Override
    public ObservableBooleanValue descriptorPresent() {
        return descriptorPresent;
    }

    @Override
    public ObservableBooleanValue valuePresent() {
        return valuePresent;
    }

    @Override
    public ObservableList<ConfigFX> getChildren() {
        return FXCollections.emptyObservableList();
    }

    Optional<ValueDescriptor> getDescriptor() {
        return Optional.ofNullable(descriptor.get());
    }

    public Value getValue() {
        return value.get();
    }

    public ObservableObjectValue<Value> valueProperty() {
        return value;
    }


    @Override
    public void remove() {
        if (valuePresent.get()) {
            getParent()
                    .flatMap(parent -> parent.getConfig())
                    .ifPresent(configuration -> configuration.removeValue(getName()));
        }
        getParent().ifPresent( parent-> parent.invalidate());
    }

    public ValueChooser valueChooser() {
        ValueChooser chooser;
        ValueCallback callback = (Value value) -> {
            setValue(value);
            invalidate();
            return new ValueCallbackResponse(true, value, "");
        };

        if (getDescriptor().isPresent()) {
            chooser = ValueChooserFactory.getInstance().build(getDescriptor().get(), getValue(), callback);
        } else {
            chooser = ValueChooserFactory.getInstance().build(getValue(), callback);
        }
//        chooser.setDisabled(!protectedProperty().get());
        return chooser;
    }

    @Override
    protected void invalidate() {
        value.invalidate();
        valuePresent.invalidate();
        super.invalidate();
    }
}
