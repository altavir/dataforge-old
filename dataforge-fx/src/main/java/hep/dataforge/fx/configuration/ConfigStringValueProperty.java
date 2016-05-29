/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.configuration;

import hep.dataforge.meta.Configuration;


public class ConfigStringValueProperty extends ConfigValuePropertyBase<String> {

    public ConfigStringValueProperty(Configuration config, String valueName) {
        super(config, valueName);
    }

    @Override
    protected String getConfigValue(Configuration cfg, String valueName) {
        return cfg.getString(valueName, "");
    }
    
}
