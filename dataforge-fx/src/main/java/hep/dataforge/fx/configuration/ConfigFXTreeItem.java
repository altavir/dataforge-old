package hep.dataforge.fx.configuration;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.meta.Configuration;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by darksnake on 01-May-17.
 */
public class ConfigFXTreeItem extends TreeItem<ConfigFX> {
    /**
     * The tag not to display node or value in configurator
     */
    public static final String NO_CONFIGURATOR_TAG = "nocfg";


    public ConfigFXTreeItem(ConfigFX value) {
        super(value);
        fillChildren();
        value.addObserver((o, arg) -> invalidate());
    }

    public ConfigFXTreeItem(ConfigFX value, Node graphic) {
        super(value, graphic);
        fillChildren();
        value.addObserver((o, arg) -> invalidate());
    }

    private Function<Configuration, NodeDescriptor> descriptorProvider;

    private void fillChildren() {
        getChildren().setAll(
                ConfigFXTreeItem.this.getValue().getChildren().stream()
                        .filter(cfg -> toShow(cfg))
                        .map(ConfigFXTreeItem::new)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public boolean isLeaf() {
        return getValue() instanceof ConfigFXValue;
    }


    private boolean toShow(ConfigFX cfg) {
        if (cfg instanceof ConfigFXNode) {
            return !((ConfigFXNode) cfg).getDescriptor().map(nd -> nd.tags().contains(NO_CONFIGURATOR_TAG)).orElse(false);
        } else if (cfg instanceof ConfigFXValue) {
            return !((ConfigFXValue) cfg).getDescriptor().map(vd -> vd.tags().contains(NO_CONFIGURATOR_TAG)).orElse(false);
        } else {
            return true;
        }
    }


    /**
     * Is this branch a root
     *
     * @return
     */
    public boolean isRoot() {
        return this.getParent() == null;
    }

    public void invalidate(){
        fillChildren();
        TreeModificationEvent<ConfigFX> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), this);
        Event.fireEvent(this, event);
    }

}
