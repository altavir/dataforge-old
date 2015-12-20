/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.meta;

import hep.dataforge.values.Value;
import java.util.List;

/**
 * A simple implementation of configurable that applies observer to
 * configuration on creation
 *
 * @author Alexander Nozik
 */
public abstract class SimpleConfigurable implements Configurable, Annotated {

    private Configuration configuration;

    /**
     * Create new configurable object and register observer that notifies this object if configuration changes
     * @param an 
     */
    public SimpleConfigurable(Meta an) {
        if(an == null){
            an = Meta.buildEmpty("config");
        }
        setConfig(an);
    }
    
    protected final void setConfig(Meta config){
        configuration = new Configuration(config);
        configuration.addObserver(new ConfigChangeListener() {

            @Override
            public void notifyValueChanged(String name, Value oldItem, Value newItem) {
                applyValueChange(name, oldItem, newItem);
            }

            @Override
            public void notifyElementChanged(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem) {
                applyElementChange(name, oldItem, newItem);
            }
        });
    }

    public SimpleConfigurable() {
        this(Meta.buildEmpty("config"));
    }

    /**
     * {@inheritDoc }
     * @return 
     */
    @Override
    public final Configuration getConfig() {
        return configuration;
    }

    /**
     * Get configuration as an immutable annotation
     * @return 
     */
    @Override
    public Meta meta() {
        return configuration;
    }
    
    /**
     * Apply the whole new configuration. It does not change configuration, merely applies changes
     * @param config 
     */
    protected abstract void applyConfig(Meta config);
    
    /**
     * Apply specific value change. By default applies the whole configuration.
     * @param name
     * @param oldItem
     * @param newItem 
     */
    protected void applyValueChange(String name, Value oldItem, Value newItem){
        applyConfig(configuration);
    }
    
    /**
     * Apply specific element change. By default applies the whole configuration.
     * @param name
     * @param oldItem
     * @param newItem 
     */
    protected void applyElementChange(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem){
        applyConfig(configuration);
    }
    
    /**
     * Add additional getConfig observer to configuration
     * @param observer 
     */
    public void addConfigObserver(ConfigChangeListener observer){
        this.configuration.addObserver(observer);
    }

    /**
     * remove additional getConfig observer from configuration
     * @param observer 
     */
    public void removeConfigObserver(ConfigChangeListener observer){
        this.configuration.addObserver(observer);
    }
    
    /**
     * Applies changes from given config to this one
     * @param config 
     */
    public void configure(Meta config){
        this.configuration.update(config);
    }
}
