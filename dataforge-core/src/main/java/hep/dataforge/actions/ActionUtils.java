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
package hep.dataforge.actions;

import hep.dataforge.cache.CachePlugin;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.FileDataFactory;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.providers.Path;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

public class ActionUtils {

    public static final String ACTION_TYPE = "type";

    public static final String ACTION_NODE_KEY = "action";

    public static final String DATA_ELEMENT = "data";

    public static final String SEQUENCE_ACTION_TYPE = "sequence";

    /**
     * хелпер для быстрого запуска действия, использующего только ввод и вывод
     *
     * @param context
     * @param config
     * @return
     */
    public static DataNode runConfig(Context context, Meta config) {
        DataNode data;
        if (config.hasMeta(DATA_ELEMENT)) {
            Meta dataElement = config.getMeta(DATA_ELEMENT);
            data = new FileDataFactory().build(context, dataElement);
        } else {
            data = null;
        }
        return runAction(context, data, config);
    }

    /**
     * Run action configuration from specific path
     *
     * @param context
     * @param path
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static DataNode runConfig(Context context, String path) throws IOException, ParseException {
        MetaBuilder config = MetaFileReader.instance().read(context, path);

        //FIXME substitution of properties and context properties should be the same
        readProperties(context, config);
        //building Meta ensures that context values are substituted
        return ActionUtils.runConfig(context, config.build());
    }

    public static void readProperties(Context context, Meta element)
            throws ContentException {
        if (element.hasMeta("property")) {
            List<? extends Meta> propertyNodes = element.getMetaList("property");
            propertyNodes.stream().forEach((option) -> {
                context.putValue(option.getString("key"), option.getString("value"));
            });
        }
    }

    public static DataNode runAction(Context context, DataNode data, Meta actionMeta) {
        String actionType = actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
        return buildAction(context, actionType).run(context, data, actionMeta);
    }

    /**
     * Search for an Action in context plugins and up the parent plugin. Throw an exception if action not found.
     * @param context
     * @param actionName
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Action<T, R> buildAction(Context context, String actionName) {
        if (SEQUENCE_ACTION_TYPE.equals(actionName)) {
            return new SequenceAction();
        } else {
            Path path = Path.of(actionName, Action.ACTION_PROVIDER_KEY);
            return context.pluginManager().stream(true)
                    .filter(plugin -> plugin.provides(path))
                    .findFirst()
                    .map(plugin -> plugin.provide(path, Action.class))
                    .orElseThrow(() -> new NameNotFoundException(actionName));
        }
    }


    /**
     * Compose two actions with complementary types into one.
     *
     * @param first
     * @param second
     * @param <T>    initial type
     * @param <I>    intermidiate type
     * @param <R>    result type
     * @return
     */
    public static <T, I, R> Action<T, R> compose(Action<T, I> first, Action<I, R> second) {
        return new Action<T, R>() {
            @Override
            public DataNode<R> run(Context context, DataNode<? extends T> data, Meta actionMeta) {
                return second.run(context, first.run(context, data, actionMeta), actionMeta);
            }

            @Override
            public String getName() {
                return first.getName() + " -> " + second.getName();
            }
        };
    }

    /**
     * Compose any number of actions.
     *
     * @param actions
     * @return
     */
    public static Action<?, ?> compose(List<Action<?, ?>> actions) {
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("Action list should not be empty");
        }
        return new Action() {
            @Override
            public DataNode run(Context context, DataNode data, Meta actionMeta) {
                DataNode result = data;
                for (Action<?, ?> action : actions) {
                    result = action.run(context, result, actionMeta);
                }
                return result;
            }

            @Override
            public String getName() {
                return actions.stream().map(it -> it.getName()).collect(Collectors.joining(" -> "));
            }
        };
    }

    public static class SequenceAction extends GenericAction {

        @Override
        public DataNode run(Context context, DataNode data, Meta sequenceMeta) {
            DataNode res = data;
            CachePlugin cache = null;
            //Set data cache if it is defined in context
            if (context.getBoolean("enableCache", false)) {
                cache = context.getFeature(CachePlugin.class);
            }

            MetaBuilder id = new MetaBuilder("action").setValue("context", context.getName());
            for (Meta actionMeta : sequenceMeta.getMetaList(ACTION_NODE_KEY)) {
                id = id.setNode("meta", actionMeta);
                String actionType = actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
                Action action = buildAction(context, actionType);
                res = action.run(context, res, actionMeta);
                if (cache != null && actionMeta.getBoolean("cacheResult", false)) {
                    //FIXME add context identity here
                    res = cache.cacheNode(action.getName(), id, res);
                }
            }
            return res;
        }

        @Override
        public String getName() {
            return SEQUENCE_ACTION_TYPE;
        }

    }
}
