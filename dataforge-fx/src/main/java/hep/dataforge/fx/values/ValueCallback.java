/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values;

import hep.dataforge.values.Value;

/**
 * A callback for some visual object trying to change some value
 * @author Alexander Nozik <altavir@gmail.com>
 */
public interface ValueCallback {
    ValueCallbackResponse update(Value value);
}
