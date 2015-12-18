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

import java.util.ServiceLoader;

/**
 * Default plugin library for framework
 * @author Alexander Nozik
 */
public class PluginLibrary extends SimpleLibrary<Plugin> {
    
    private static final PluginLibrary INSTANCE = new PluginLibrary();
    
    private PluginLibrary() {
    }
    
    public static PluginLibrary getInstance() {
        return INSTANCE;
    }

    /**
     * Collect all modules from ClassPath registered as services. 
     */
    public static void update(){
        ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class);
        for(Plugin plugin : pluginLoader){
            INSTANCE.put(plugin.getTag(), plugin);
        }
    }

    @Override
    public boolean has(VersionTag tag) {
        update();
        return super.has(tag); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Plugin get(VersionTag tag) {
        update();
        return super.get(tag); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
