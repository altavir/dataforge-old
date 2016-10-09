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
package hep.dataforge.data;

import hep.dataforge.context.Context;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;

import static hep.dataforge.data.DataFactory.*;

/**
 * A factory for data tree
 *
 * @author Alexander Nozik
 */
@NodeDef(name = NODE_META_KEY, info = "node meta-data")
@NodeDef(name = NODE_KEY)
@ValueDef(name = NODE_NAME_KEY, info = "Node or data name")
@ValueDef(name = NODE_TYPE_KEY, info = "Node or data type")
public abstract class DataFactory<T> {

    public static final String NODE_META_KEY = "meta";
    public static final String NODE_TYPE_KEY = "type";
    public static final String NODE_KEY = "node";
    public static final String NODE_NAME_KEY = "name";

    private final Class<T> baseType;

    protected DataFactory(Class<T> baseType) {
        this.baseType = baseType;
    }

    public DataNode<T> build(Context context, Meta dataConfig) {
        return builder(context, dataConfig).build();
    }

    protected DataTree.Builder<T> builder(Context context, Meta dataConfig) {
//        Class type;
//        if (dataConfig.hasValue(NODE_TYPE_KEY)) {
//            try {
//                type = Class.forName(dataConfig.getString(NODE_TYPE_KEY));
//            } catch (ClassNotFoundException ex) {
//                throw new RuntimeException("Can't initialize data node", ex);
//            }
//        } else {
//            type = Object.class;
//        }
        DataTree.Builder<T> builder = DataTree.builder(baseType);
        fillData(context, dataConfig, builder);
        return builder;
    }

    /**
     * Fill data to existing builder. Useful for custom filtering
     *
     * @param context
     * @param dataConfig
     * @param builder
     * @return
     */
    protected void fillData(Context context, Meta dataConfig, DataTree.Builder<T> builder) {
        // Apply node name
        if (dataConfig.hasMeta(NODE_NAME_KEY)) {
            builder.setName(dataConfig.getString(NODE_NAME_KEY));
        }

        // Apply node meta
        if (dataConfig.hasMeta(NODE_META_KEY)) {
            builder.setMeta(dataConfig.getMeta(NODE_META_KEY));
        }

        // Apply non-specific child nodes
        if (dataConfig.hasMeta(NODE_KEY)) {
            //FIXME check types for child nodes
            dataConfig.getMetaList(NODE_KEY).forEach((Meta nodeMeta) -> builder.putNode(build(context, nodeMeta)));
        }

        //Configuring filter
        DataFilter filter = new DataFilter();
        filter.configure(dataConfig);
        // Apply child nodes specific to this factory
        buildChildren(context, builder, filter, dataConfig);
    }

    /**
     * Apply children nodes and data elements to the builder. Inheriting classes
     * can add their own children builders.
     *
     * @param builder
     * @param meta
     */
    protected abstract void buildChildren(Context context, DataTree.Builder<T> builder, DataFilter filter, Meta meta);

}
