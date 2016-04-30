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
import static hep.dataforge.data.DataFactory.*;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.utils.MetaFactory;

/**
 * A replacement for obsolete ImportDataAction
 *
 * @author Alexander Nozik
 */
@NodeDef(name = DATA_META_KEY, info = "node meta-data")
@NodeDef(name = DATA_NODE_KEY)
@ValueDef(name = NODE_NAME_KEY, info = "Node or data name")
@ValueDef(name = TYPE_VALUE_KEY, info = "Node or data type")
public abstract class DataFactory implements MetaFactory<DataTree> {

    public static final String DATA_META_KEY = "meta";
    public static final String TYPE_VALUE_KEY = "type";
    public static final String DATA_NODE_KEY = "node";
    public static final String NODE_NAME_KEY = "name";

    @Override
    public DataTree build(Context context, Meta dataConfig) {
        Class type;
        if (dataConfig.hasValue(TYPE_VALUE_KEY)) {
            try {
                type = Class.forName(dataConfig.getString(TYPE_VALUE_KEY));
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Can't initialize data node", ex);
            }
        } else {
            type = Object.class;
        }
        DataTree.Builder<?> builder = DataTree.builder(type);

        // Apply node name
        if (dataConfig.hasNode(NODE_NAME_KEY)) {
            builder.setName(dataConfig.getString(NODE_NAME_KEY));
        }

        // Apply node type
        if (dataConfig.hasNode(DATA_META_KEY)) {
            builder.setMeta(dataConfig.getNode(DATA_META_KEY));
        }

        // Apply non-specific child nodes
        if (dataConfig.hasNode(DATA_NODE_KEY)) {
            dataConfig.getNodes(DATA_NODE_KEY).forEach((Meta nodeMeta) -> builder.putNode(build(context, nodeMeta)));
        }

        // Apply child nodes specific to this factory
        buildChildren(context, builder, dataConfig);

        return builder.build();
    }

    /**
     * Apply children nodes and data elements to the builder. Inheriting classes
     * can add their own children builders.
     *
     * @param builder
     * @param meta
     */
    protected abstract void buildChildren(Context context, DataTree.Builder<?> builder, Meta meta);
}
