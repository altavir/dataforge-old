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
@NodeDef(name = NODE_META_KEY, info = "Node meta-data")
@NodeDef(name = NODE_KEY, info = "Recursively add node to the builder")
@NodeDef(name = FILTER_KEY, from = "hep.dataforge.data.CustomDataFilter", info = "Filter definition to be applied after node construction is finished")
@NodeDef(name = ITEM_KEY, from = "method::hep.dataforge.data.DataFactory.buildData", info = "A fixed context-based node with or without actual static data")
@ValueDef(name = NODE_NAME_KEY, info = "Node or data name")
@ValueDef(name = NODE_TYPE_KEY, info = "Node or data type")
public class DataFactory<T> implements DataLoader<T> {

    public static final String NODE_META_KEY = "meta";
    public static final String NODE_TYPE_KEY = "type";
    public static final String NODE_KEY = "node";
    public static final String ITEM_KEY = "item";
    public static final String NODE_NAME_KEY = "name";
    public static final String FILTER_KEY = "filter";

    private final Class<T> baseType;

    protected DataFactory(Class<T> baseType) {
        this.baseType = baseType;
    }

    @Override
    public DataNode<T> build(Context context, Meta meta) {
        //Creating filter
        CustomDataFilter filter = new CustomDataFilter(meta.getMetaOrEmpty(FILTER_KEY));

        DataTree<T> tree = builder(context,meta).build();
        //Applying filter if needed
        if (filter.meta().isEmpty()) {
            return tree;
        } else {
            return filter.filter(tree);
        }
    }

    /**
     * Return DataTree.Builder after node fill but before filtering. Any custom logic should be applied after it.
     *
     * @param context
     * @param dataConfig
     * @return
     */
    protected DataTree.Builder<T> builder(Context context, Meta dataConfig) {
        DataTree.Builder<T> builder = DataTree.builder(baseType);

        // Apply node name
        if (dataConfig.hasValue(NODE_NAME_KEY)) {
            builder.setName(dataConfig.getString(NODE_NAME_KEY));
        }

        // Apply node meta
        if (dataConfig.hasMeta(NODE_META_KEY)) {
            builder.setMeta(dataConfig.getMeta(NODE_META_KEY));
        }

        // Apply non-specific child nodes
        if (dataConfig.hasMeta(NODE_KEY)) {
            dataConfig.getMetaList(NODE_KEY).forEach((Meta nodeMeta) -> {
                //FIXME check types for child nodes
                DataNode<T> node = build(context, nodeMeta);
                builder.putNode(node);
            });
        }

        //Add custom items
        if (dataConfig.hasMeta(ITEM_KEY)) {
            dataConfig.getMetaList(ITEM_KEY).forEach(itemMeta -> builder.putData(buildData(context, itemMeta)));
        }

        // Apply child nodes specific to this factory
        fill(builder, context, dataConfig);
        return builder;
    }

    @ValueDef(name = NODE_NAME_KEY, required = true, info = "The name for this data item")
    @ValueDef(name = "path", info = "The context path for the object to be included as data. Chain path is supported")
    @NodeDef(name = NODE_META_KEY, info = "Meta for this item")
    protected NamedData<? extends T> buildData(Context context, Meta itemMeta) {
        String name = itemMeta.getString(NODE_NAME_KEY);

        T obj = itemMeta.optValue("path")
                .flatMap(path -> context.provide(path.stringValue(), baseType))
                .orElse(null);

        Meta meta = itemMeta.optMeta(NODE_META_KEY).orElse(Meta.empty());

        return NamedData.buildStatic(name, obj, meta);
    }

    /**
     * Apply children nodes and data elements to the builder. Inheriting classes
     * can add their own children builders.
     *
     * @param builder
     * @param meta
     */
    protected void fill(DataTree.Builder<T> builder, Context context, Meta meta) {
        //Do nothing for default factory
    }

    @Override
    public String getName() {
        return "default";
    }
}
