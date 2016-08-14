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
package hep.dataforge.stat.models;

import hep.dataforge.context.Context;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.ContextMetaFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The library of available models
 * <p>
 * TODO transform ModelManager into Library
 * </p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ModelManager {

    /**
     * Constant <code>MODEL_NAME="name"</code>
     */
    public static String MODEL_NAME = "modelName";
    private final HashMap<String, ContextMetaFactory<Model>> modelList = new HashMap<>();
    private final HashMap<String, ModelDescriptor> descriptorList = new HashMap<>();
    private ModelManager defaultManager;

    public ModelManager(ModelManager defaultManager) {
        this.defaultManager = defaultManager;
    }

    public ModelManager() {
    }

    public void addModel(String name, ContextMetaFactory<Model> mf) {
        modelList.put(name.toLowerCase(), mf);
    }

    public void addModel(String name, ContextMetaFactory<Model> mf, ModelDescriptor descriptor) {
        modelList.put(name.toLowerCase(), mf);
        descriptorList.put(name.toLowerCase(), descriptor);
    }

    /**
     * <p>
     * listDescriptors.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, ModelDescriptor> listDescriptors() {
        return descriptorList;
    }

    @ValueDef(name = "modelName", info = "The name of the pre-loaded model to use.")
    public Model buildModel(Context context, Meta a) {
        String modelName = a.getString(MODEL_NAME);
        ContextMetaFactory<Model> factory = modelList.get(modelName.toLowerCase());
        if (factory == null) {
            if (defaultManager == null) {
                throw new NameNotFoundException(modelName);
            } else {
                return defaultManager.buildModel(context, a);
            }
        }
        return factory.build(context, a);
    }

    public Model buildModel(Context context, String name) throws NameNotFoundException {
        ContextMetaFactory<Model> factory = modelList.get(name.toLowerCase());
        if (factory == null) {
            if (defaultManager == null) {
                throw new NameNotFoundException(name);
            } else {
                return defaultManager.buildModel(context, name);
            }
        }
        return factory.build(context, Meta.buildEmpty("model"));
    }

    /**
     * <p>
     * getModelNameList.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getModelNameList() {
        return modelList.keySet();
    }
}
