package hep.dataforge.fx.configuration;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Tree item for node
 * Created by darksnake on 30-Apr-17.
 */
public class ConfigFXNode extends ConfigFX {

    /**
     * Create a new ConfigFX node based on parent. The name must not be empty
     *
     * @param parent
     * @param name
     * @return
     */
    public static ConfigFXNode build(@NotNull ConfigFXNode parent, @NotNull String name) {
        ConfigFXNode res = new ConfigFXNode();
        res.nameProperty().setValue(name);
        res.parentProperty.setValue(parent);
        return res;
    }

    public static ConfigFXNode build(@NotNull Configuration config, @Nullable NodeDescriptor descriptor) {
        ConfigFXNode res = new ConfigFXNode();
        res.nameProperty().setValue(config.getName());
        res.configOverride.setValue(config);
        res.descriptorOverride.setValue(descriptor);
        return res;
    }


    private ObjectProperty<NodeDescriptor> descriptorOverride = new SimpleObjectProperty<>(null);
    private ObjectProperty<Configuration> configOverride = new SimpleObjectProperty<>(null);

    private ObservableObjectValue<NodeDescriptor> descriptor = new ObjectBinding<NodeDescriptor>() {
        {
            bind(parentProperty, name, descriptorOverride);
        }

        @Override
        protected NodeDescriptor computeValue() {
            return Optionals.either(Optional.ofNullable(descriptorOverride.get()))
                    .or(() -> getParent()
                            .flatMap(ConfigFXNode::getDescriptor)
                            .map(descriptor -> descriptor.childDescriptor(getName()))
                    ).opt().orElse(null);
        }
    };

    private ObjectBinding<Configuration> configuration = new ObjectBinding<Configuration>() {
        {
            bind(parentProperty, name, configOverride);
        }

        //TODO add numbered node resolution
        @Override
        protected Configuration computeValue() {
            return Optionals.either(Optional.ofNullable(configOverride.get()))
                    .or(() -> getParent()
                            .flatMap(ConfigFXNode::getConfig)
                            .flatMap(parentConfig -> parentConfig.optMeta(getName()))
                    ).opt().orElse(null);
        }
    };

    private ObservableStringValue description = new StringBinding() {
        {
            bind(descriptor);
        }

        @Override
        protected String computeValue() {
            NodeDescriptor d = descriptor.get();
            return d == null ? "" : d.info();
        }
    };

    private ObservableBooleanValue configurationPresent = Bindings.isNotNull(configuration);

    private ObservableBooleanValue descriptorPresent = Bindings.isNotNull(descriptor);

    private ConfigFXNode() {

    }


    /**
     * Get existing configuration node or create and attach new one
     *
     * @return
     */
    public synchronized Configuration getOrBuildNode() {
        return getConfig().orElseGet(() -> {
            if (!getParent().isPresent()) {
                throw new RuntimeException("The configuration for root node is note defined");
            }
            Configuration cfg = new Configuration(getName());
            getParent().get().getOrBuildNode().attachNode(cfg);
            invalidate();
            return cfg;
        });
    }

    //getters

    @Override
    public ObservableList<ConfigFX> getChildren() {
        //return children;
        ObservableList<ConfigFX> list = FXCollections.observableArrayList();
        Set<String> nodeNames = new HashSet<>();
        Set<String> valueNames = new HashSet<>();
        //Adding all existing values and nodes
        getConfig().ifPresent(config -> {
                    config.getNodeNames().forEach(childNodeName -> {
                        nodeNames.add(childNodeName);
                        int nodeSize = config.getMetaList(childNodeName).size();
                        if (nodeSize == 1) {
                            list.add(ConfigFXNode.build(ConfigFXNode.this, childNodeName));
                        } else {
                            for (int i = 0; i < nodeSize; i++) {
                                list.add(ConfigFXNode.build(ConfigFXNode.this, childNodeName + "[" + i + "]"));
                            }
                        }
                    });

                    config.getValueNames().forEach(childValueName -> {
                        valueNames.add(childValueName);
                        list.add(ConfigFXValue.build(ConfigFXNode.this, childValueName));
                    });
                }
        );

        // adding nodes and values from descriptor
        getDescriptor().ifPresent(desc -> {
                    desc.childrenDescriptors().keySet().forEach(nodeName -> {
                        //Adding only those nodes, that have no configuration of themselves
                        if (!nodeNames.contains(nodeName)) {
                            list.add(ConfigFXNode.build(ConfigFXNode.this, nodeName));
                        }
                    });
                    desc.valueDescriptors().keySet().forEach(valueName -> {
                        //Adding only those nodes, that have no configuration of themselves
                        if (!valueNames.contains(valueName)) {
                            list.add(ConfigFXValue.build(ConfigFXNode.this, valueName));
                        }
                    });
                }
        );

        return list;
    }


    Optional<Configuration> getConfig() {
        return Optional.ofNullable(configuration.get());
    }

    Optional<NodeDescriptor> getDescriptor() {
        return Optional.ofNullable(descriptor.get());
    }

    public ObjectProperty<NodeDescriptor> descriptorOverrideProperty() {
        return descriptorOverride;
    }

    public ObservableObjectValue<NodeDescriptor> descriptorProperty() {
        return descriptor;
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
        return configurationPresent;
    }

    public void addValue(String name, Value value) {
        getOrBuildNode().putValue(name, value);
        invalidate();
    }

    public void addNode(String name) {
        getOrBuildNode().putNode(name, Meta.empty());
        invalidate();
    }

    @Override
    public void remove() {
        if (configurationPresent.get()) {
            getParent()
                    .flatMap(parent -> parent.getConfig())
                    .ifPresent(configuration -> configuration.removeNode(getName()));
            getParent().ifPresent( parent-> parent.invalidate());
        }
    }

    @Override
    protected void invalidate() {
        configuration.invalidate();
        super.invalidate();
    }
}
