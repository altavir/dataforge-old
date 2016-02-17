/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Meta;
import java.util.List;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class MetaTreeItem extends TreeItem<MetaTree> {

    public MetaTreeItem(Configuration config, NodeDescriptor descriptor) {
        super(new MetaTreeBranch(null, config, descriptor));
    }

    public MetaTreeItem(MetaTree value) {
        super(value);
    }

    public MetaTreeItem(MetaTree value, Node graphic) {
        super(value, graphic);
    }

    private Function<Configuration, NodeDescriptor> descriptorProvider;

    private boolean invalid = true;

    @Override
    public ObservableList<TreeItem<MetaTree>> getChildren() {
        if (invalid) {
            invalid = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        return !getValue().isNode();
    }

    /**
     * Generate a subtree
     *
     * @param item
     * @return
     */
    private ObservableList<TreeItem<MetaTree>> buildChildren(MetaTreeItem item) {
        if (isLeaf()) {
            return FXCollections.emptyObservableList();
        } else {
            ObservableList<TreeItem<MetaTree>> children = FXCollections.observableArrayList();
            MetaTreeBranch branch = (MetaTreeBranch) item.getValue();
            Configuration config = branch.getNode();
            NodeDescriptor descriptor = branch.getDescriptor();

            // recursevely adding child nodes including descriptors
            if (config != null) {
                for (String childNodeName : config.getNodeNames()) {
                    List<Configuration> childConfigs = config.getNodes(childNodeName);
                    childConfigs.stream().map((childConfig) -> {
                        //applying descriptor from parent
                        NodeDescriptor childDescriptor = descriptor == null ? null : descriptor.childDescriptor(childNodeName);
                        //applying descriptor from external provider if it is present
                        if (childDescriptor == null && descriptorProvider != null) {
                            childDescriptor = descriptorProvider.apply(childConfig);
                        }
                        MetaTree childTree = new MetaTreeBranch(branch, childConfig, childDescriptor);
                        return childTree;
                    }).forEach((childTree) -> {
                        children.add(new MetaTreeItem(childTree));
                    });
                }

                // adding values with descriptors if available
                config.getValueNames().stream()
                        .map((valueName) -> new MetaTreeLeaf(branch, valueName))
                        .forEach((valueLeaf) -> {
                            children.add(new MetaTreeItem(valueLeaf));
                        });
            }

            // adding the rest default value from descriptor. Ignoring the ones allready added
            if (descriptor != null) {
                descriptor.childrenDescriptors().values().stream()
                        .filter((nd) -> (config == null || !config.hasNode(nd.getName())))
                        .map((nd) -> new MetaTreeBranch(branch, null, nd))
                        .forEach((childTree) -> {
                            children.add(new MetaTreeItem(childTree));
                        });

                descriptor.valueDescriptors().values().stream()
                        .filter((vd) -> (config == null || !config.hasValue(vd.getName())))
                        .map((vd) -> new MetaTreeLeaf(branch, vd.getName()))
                        .forEach((childTree) -> {
                            children.add(new MetaTreeItem(childTree));
                        });
            }

            return children;
        }
    }

    public void addBranch(String name) {
        addBranch(Meta.buildEmpty(name), null);
    }

    /**
     * Add node branch to this branch
     *
     * @param node
     * @param descriptor
     */
    public void addBranch(Meta node, NodeDescriptor descriptor) {
        if (isLeaf()) {
            LoggerFactory.getLogger(getClass()).error("Trying to add branch to leaf element.");
        } else {
            Configuration childNode = new Configuration(node);
            MetaTreeBranch branch = (MetaTreeBranch) getValue();
            branch.getNode().attachNode(childNode);
            getChildren().add(new MetaTreeItem(new MetaTreeBranch(branch, childNode, descriptor)));
        }
    }

    public void addLeaf(String name) {
        addLeaf(name, null);
    }

    /**
     * Add value leaf to this branch
     *
     * @param name
     */
    public void addLeaf(String name, Object value) {
        if (isLeaf()) {
            LoggerFactory.getLogger(getClass()).error("Trying to add leaf to leaf element.");
        } else {
            MetaTreeBranch branch = (MetaTreeBranch) getValue();
            branch.getNode().setValue(name, value);
            getChildren().add(new MetaTreeItem(new MetaTreeLeaf(branch, name)));
        }
    }

    /**
     * Remove this branch or leaf
     */
    public void remove() {
        if (this.isRoot()) {
            LoggerFactory.getLogger(getClass()).error("Can't remove root node");
        } else if (getValue().isDefault().get()) {
            LoggerFactory.getLogger(getClass()).error("Can't remove default node");
        } else {
            if (isLeaf()) {
                ((MetaTreeBranch) this.getParent().getValue()).getNode().removeValue(getValue().getName());
            } else {
                ((MetaTreeBranch) this.getParent().getValue()).getNode()
                        .removeChildNode(((MetaTreeBranch) getValue()).getNode());
            }
            if (!getValue().hasDescriptor()) {
                getParent().getChildren().remove(this);
            } else {
                Event.fireEvent(getParent(), new TreeModificationEvent(valueChangedEvent(), this));
            }
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

    /**
     * Invalidate current tree. Use with care because it could break custom
     * descriptor bindings.
     */
    public void invalidate() {
        this.invalid = true;
    }

    public void setDescriptorProvider(Function<Configuration, NodeDescriptor> descriptorProvider) {
        this.descriptorProvider = descriptorProvider;
        invalidate();
    }

}
