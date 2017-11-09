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

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.names.Name;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A simple implementation of configurable that applies observer to
 * configuration on creation
 *
 * @author Alexander Nozik
 */
public class SimpleConfigurable implements Configurable, Metoid {

    private Configuration configuration = null;


    /**
     * Create a pre-configured instance
     * @param configuration
     */
    public SimpleConfigurable(Configuration configuration) {
        this.configuration = configuration;
    }


    public SimpleConfigurable() {
    }

    /**
     * {@inheritDoc }
     *
     * @return
     */
    @Override
    public final Configuration getConfig() {
        if (configuration == null) {
            initConfig();
        }
        return configuration;
    }

    /**
     * Get configuration as an immutable annotation underplayed by defaults from class description
     *
     * @return
     */
    @Override
    public Meta meta() {
        return new Laminate(getConfig()).withDescriptor(DescriptorUtils.buildDescriptor(getClass()));
    }

    /**
     * Apply the whole new configuration. It does not change configuration,
     * merely applies changes
     *
     * @param config
     */
    protected void applyConfig(Meta config){
        //does nothing by default
    }

    /**
     * Apply specific value change. By default applies the whole configuration.
     *
     * @param name
     * @param oldItem
     * @param newItem
     */
    protected void applyValueChange(String name, Value oldItem, Value newItem) {
        applyConfig(getConfig());
    }

    /**
     * Apply specific element change. By default applies the whole
     * configuration.
     *
     * @param name
     * @param oldItem
     * @param newItem
     */
    protected void applyElementChange(String name, List<? extends Meta> oldItem, List<? extends Meta> newItem) {
        applyConfig(getConfig());
    }

    /**
     * Add additional getConfig observer to configuration
     *
     * @param observer
     */
    public void addConfigObserver(ConfigChangeListener observer) {
        this.getConfig().addObserver(observer);
    }

    /**
     * remove additional getConfig observer from configuration
     *
     * @param observer
     */
    public void removeConfigObserver(ConfigChangeListener observer) {
        this.getConfig().addObserver(observer);
    }

    /**
     * validate incoming configuration changes and return correct version (all
     * invalid values are excluded). By default just returns unchanged
     * configuration.
     *
     * @param config
     * @return
     */
    protected Meta validate(Meta config) {
        return config;
    }

    private void initConfig() {
        if (configuration == null) {
            configuration = new Configuration("");
        }
        configuration.addObserver(new ConfigChangeListener() {

            @Override
            public void notifyValueChanged(Name name, Value oldItem, Value newItem) {
                applyValueChange(name.toUnescaped(), oldItem, newItem);
            }

            @Override
            public void notifyNodeChanged(Name name, @NotNull List<? extends Meta> oldItem, @NotNull List<? extends Meta> newItem) {
                applyElementChange(name.toUnescaped(), oldItem, newItem);
            }
        });
    }

    /**
     * Applies changes from given config to this one
     *
     * @param config
     */
    @Override
    public Configurable configure(Meta config) {
        //Check and correct input configuration
        getConfig().update(config);
        applyConfig(getConfig());
        return this;
    }
}
