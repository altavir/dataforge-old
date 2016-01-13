/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.values.Value;
import javafx.scene.Node;

/**
 * A value chooser object. Must have an empty constructor to be invoked by
 * reflections.
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public interface ValueChooser {

    void setValueCallback(ValueCallback callback);

    void updateValue(Value value);

    /**
     * Get or create a Node that could be later inserted into some parent
     * object.
     *
     * @return
     */
    Node getNode();

}
