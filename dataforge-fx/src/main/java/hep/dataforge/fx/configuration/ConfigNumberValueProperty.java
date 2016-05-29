/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration;

import hep.dataforge.meta.Configuration;
import hep.dataforge.values.Value;


public class ConfigNumberValueProperty extends ConfigValuePropertyBase<Number> {

    public ConfigNumberValueProperty(Configuration config, String valueName) {
        super(config, valueName);
    }

    @Override
    protected Number getConfigValue(Configuration cfg, String valueName) {
        return cfg.getValue(valueName, Value.NULL).numberValue();
    }
    
}
