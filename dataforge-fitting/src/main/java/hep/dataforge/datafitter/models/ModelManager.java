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
package hep.dataforge.datafitter.models;

import hep.dataforge.context.Context;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaFactory;
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

    private ModelManager defaultManager;

    private final HashMap<String, MetaFactory<Model>> modelList = new HashMap<>();
    private final HashMap<String, ModelDescriptor> descriptorList = new HashMap<>();

    /**
     * <p>
     * Constructor for ModelManager.</p>
     *
     * @param defaultManager a
     * {@link hep.dataforge.datafitter.models.ModelManager} object.
     */
    public ModelManager(ModelManager defaultManager) {
        this.defaultManager = defaultManager;
    }

    /**
     * <p>
     * Constructor for ModelManager.</p>
     */
    public ModelManager() {
    }

    /**
     * <p>
     * addModel.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param mf a {@link hep.dataforge.utils.MetaFactory} object.
     */
    public void addModel(String name, MetaFactory<Model> mf) {
        modelList.put(name.toLowerCase(), mf);
    }

    /**
     * <p>
     * addModel.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param mf a {@link hep.dataforge.utils.MetaFactory} object.
     * @param descriptor a
     * {@link hep.dataforge.datafitter.models.ModelDescriptor} object.
     */
    public void addModel(String name, MetaFactory<Model> mf, ModelDescriptor descriptor) {
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

    /**
     * <p>
     * buildModel.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param a a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.datafitter.models.Model} object.
     */
    @ValueDef(name = "modelName", info = "The name of the preloaded model to use.")
    public Model buildModel(Context context, Meta a) {
        String modelName = a.getString(MODEL_NAME);
        MetaFactory<Model> factory = modelList.get(modelName.toLowerCase());
        if (factory == null) {
            if (defaultManager == null) {
                throw new NameNotFoundException(modelName);
            } else {
                return defaultManager.buildModel(context, a);
            }
        }
        return factory.build(context, a);
    }

    /**
     * <p>
     * buildModel.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.models.Model} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public Model buildModel(Context context, String name) throws NameNotFoundException {
        MetaFactory<Model> factory = modelList.get(name.toLowerCase());
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
