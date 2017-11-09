/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.values

import hep.dataforge.values.Value


class ValueCallbackResponse(
        /**
         * Set value success
         */
        internal var success: Boolean,
        /**
         * Value after change
         */
        internal var value: Value,
        /**
         * Message on unsuccessful change
         */
        internal var message: String)

/**
 * A callback for some visual object trying to change some value
 * @author [Alexander Nozik](mailto:altavir@gmail.com)
 */
typealias ValueCallback = (Value) -> ValueCallbackResponse

