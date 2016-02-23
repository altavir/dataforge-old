/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.dependencies;

import hep.dataforge.navigation.Provider;
import java.util.Map;

/**
 *
 * @author Alexander Nozik
 */
public interface DataTreeNode<T> extends DataNode<T>, Provider {

    /**
     * Parent of this node.
     *
     * @return
     */
    DataNode<?> getParent();

    Map<String, DataNode<? extends T>> subSets();
}
