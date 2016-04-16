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

import hep.dataforge.context.Context;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.FileDataFactory;

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
        if (config.hasNode(DATA_ELEMENT)) {
            Meta dataElement = config.getNode(DATA_ELEMENT);
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
        if (element.hasNode("property")) {
            List<? extends Meta> propertyNodes = element.getNodes("property");
            propertyNodes.stream().forEach((option) -> {
                context.putValue(option.getString("key"), option.getString("value"));
            });
        }
    }

    public static DataNode runAction(Context context, DataNode data, Meta actionMeta) {
        String actionType = actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
        return buildAction(context, actionType).run(context, data, actionMeta);
    }

    //TODO move to ActionManager
    public static Action buildAction(Context context, String actionType) {
        Action res;
        if (SEQUENCE_ACTION_TYPE.equals(actionType)) {
            res = new SequenceAction();
        } else {
            res = ActionManager.buildFrom(context).getAction(actionType);
        }
        return res;
    }

    public static class SequenceAction implements Action {

        @Override
        public DataNode run(Context context, DataNode data, Meta sequenceMeta) {
            DataNode res = data;
            for (Meta actionMeta : sequenceMeta.getNodes(ACTION_NODE_KEY)) {
                String actionType = actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
                res = buildAction(context, actionType).run(context, res, actionMeta);
            }
            return res;
        }

        @Override
        public String getName() {
            return SEQUENCE_ACTION_TYPE;
        }
    }

}
