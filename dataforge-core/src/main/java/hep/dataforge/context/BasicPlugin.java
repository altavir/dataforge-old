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
package hep.dataforge.context;

import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.names.Name;
import hep.dataforge.providers.AbstractProvider;
import hep.dataforge.providers.Path;

/**
 * A base for plugin implementation
 *
 * @author Alexander Nozik
 */
public abstract class BasicPlugin extends SimpleConfigurable implements Plugin {

    private Context context;

    private final ProviderDelegate providerDelegate = new ProviderDelegate();

    @Override
    public PluginTag[] dependsOn() {
        PluginTag tag = getTag();
        if (tag.hasValue("dependsOn")) {
            String[] strDeps = tag.getStringArray("dependsOn");
            PluginTag[] deps = new PluginTag[strDeps.length];
            for (int i = 0; i < strDeps.length; i++) {
                deps[i] = PluginTag.fromString(strDeps[i]);
            }
            return deps;
        } else {
            return new PluginTag[]{};
        }
    }

    /**
     * If tag is not defined, then the name of class is used
     *
     * @return
     */
    @Override
    public PluginTag getTag() {
        return Plugin.resolveTag(getClass());
    }

    public String getDescription() {
        return meta().getString("description", "");
    }

    /**
     * Load this plugin to the Global without annotation
     */
    public void startGlobal() {
        if (getContext() != null && !Global.instance().equals(getContext())) {
            Global.instance().getLogger().warn("Loading plugin as global from non-global context");
        }
        Global.instance().pluginManager().load(this);
    }

    @Override
    public void attach(Context context) {
        this.context = context;
    }

    @Override
    public void detach() {
        this.context = null;
    }

    @Override
    public final Context getContext() {
        return context;
    }

    @Override
    public final Object provide(Path path) {
        return providerDelegate.provide(path);
    }

    @Override
    public final boolean provides(Path path) {
        return providerDelegate.provides(path);
    }

    protected boolean provides(String target, Name name) {
        return false;
    }

    protected Object provide(String target, Name name) {
        return null;
    }

    private class ProviderDelegate extends AbstractProvider {

        @Override
        protected boolean provides(String target, Name name) {
            return BasicPlugin.this.provides(target, name);
        }

        @Override
        protected Object provide(String target, Name name) {
            return BasicPlugin.this.provide(target, name);
        }
    }


}
