/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration;

import hep.dataforge.meta.Configuration;


public class ConfigBooleanValueProperty extends ConfigValuePropertyBase<Boolean> {

    public ConfigBooleanValueProperty(Configuration config, String valueName) {
        super(config, valueName);
    }

    @Override
    protected Boolean getConfigValue(Configuration cfg, String valueName) {
        return cfg.getBoolean(valueName, false);
    }
    
}
