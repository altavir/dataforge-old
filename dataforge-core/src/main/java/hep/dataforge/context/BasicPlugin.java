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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.SimpleConfigurable;

/**
 * A base for plugin implementation
 *
 * @author Alexander Nozik
 */
public abstract class BasicPlugin extends SimpleConfigurable implements Plugin {

    private Context context;

    public BasicPlugin() {
        super.configure(getDefinition());
    }

    private Meta getDefinition() {
        MetaBuilder builder = new MetaBuilder("plugin");
        if (getClass().isAnnotationPresent(PluginDef.class)) {
            PluginDef def = getClass().getDeclaredAnnotation(PluginDef.class);
            builder.putValue("group", def.group());
            builder.putValue("name", def.name());
            builder.putValue("description", def.description());
            builder.putValue("version", def.version());
            for (String dep : def.dependsOn()) {
                builder.putValue("dependsOn", dep);
            }
        }
        return builder.build();
    }

    @Override
    protected void applyConfig(Meta config) {

    }

    @Override
    public VersionTag[] dependsOn() {
        if (meta().hasValue("dependsOn")) {
            String[] strDeps = meta().getStringArray("dependsOn");
            VersionTag[] deps = new VersionTag[strDeps.length];
            for (int i = 0; i < strDeps.length; i++) {
                deps[i] = VersionTag.fromString(strDeps[i]);
            }
            return deps;
        } else {
            return new VersionTag[]{};
        }
    }

    /**
     * If tag is not defined, than the name of class is used
     *
     * @return
     */
    @Override
    public VersionTag getTag() {
        return new VersionTag(meta().getString("group", VersionTag.DEFAULT_GROUP),
                meta().getString("name", getClass().getSimpleName()),
                meta().getString("version", VersionTag.UNVERSIONED));
    }

    public String getDescription() {
        return meta().getString("description", "");
    }

    /**
     * Load this plugin to the GlobalContext without annotation
     */
    public void startGlobal() {
        if (getContext() != null && !GlobalContext.instance().equals(getContext())) {
            GlobalContext.instance().getLogger().warn("Loading plugin as global from non-global context");
        }
        GlobalContext.instance().pluginManager().loadPlugin(this);
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


}
