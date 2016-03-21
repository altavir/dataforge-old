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
import hep.dataforge.data.DataFactory;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.FileDataFactory;

/**
 * <p>
 * RunManager class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class RunManager {

    /**
     * Constant <code>ACTION_TYPE="type"</code>
     */
    public static final String ACTION_TYPE = "type";
    /**
     * Constant <code>ACTION_LABEL="action"</code>
     */
    public static final String ACTION_LABEL = "action";
    
    public static final String DATA_ELEMENT = "data";

    /**
     * хелпер для быстрого запуска действия, использующего только ввод и вывод
     *
     * @param context
     * @param actionRoot
     * @return
     */
    public static DataNode executeAction(Context context, Meta actionRoot) {
        Action action = readAction(context, actionRoot);

        DataNode data;

        if (actionRoot.hasNode(DATA_ELEMENT)) {
            Meta dataElement = actionRoot.getNode(DATA_ELEMENT);
            data = new FileDataFactory().build(context, dataElement);
        } else {
            data = null;
        }
        return action.run(data).compute();
    }

    public static DataNode executeAction(Context context, String path) throws IOException, ParseException {
        MetaBuilder config = MetaFileReader.instance().read(context, path);

        //FIXME substitution of properties and context properties should be the same
        readProperties(context, config);
        //building Meta ensures that context values are substituted
        return executeAction(context, config.build());
    }

    
    /**
     * считываем action или actionlist в зависимости от того, что на входе
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param config a {@link hep.dataforge.meta.Meta} object.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     * @throws hep.dataforge.exceptions.WrongContentTypeException
     * @return a {@link hep.dataforge.actions.Action} object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    public static Action readAction(Context context, Meta config) throws NameNotFoundException, ContentException {
        Action res;
        // если значение type - пустое, считаем, что это последовательность
        String actionType = config.getString(ACTION_TYPE, "sequence");
        if ("sequence".equals(actionType)) {
            if (config.hasNode(ACTION_LABEL)) {
                List<? extends Meta> cfgList = config.getNodes(ACTION_LABEL);
                if (cfgList.isEmpty()) {
                    throw new RuntimeException("No action definitions are provided");
                }
                res = readAction(context, cfgList.get(0));

                for (int i = 1; i < cfgList.size(); i++) {
                    res = composite(res, readAction(context, cfgList.get(i)));
                }
            } else {
                throw new ContentException("Can't build sequence from empty action declaration list");
            }
        } else {
            res = ActionManager.buildFrom(context).buildAction(actionType, context, config);
        }

        return res;
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

    /**
     * Create a composite action where last action is attached after first one
     *
     * @param first
     * @param last
     * @return
     */
    public static Action composite(Action first, Action last) {
        return new Action() {
            @Override
            public DataNode run(DataNode res) {
                return last.run(first.run(res));
            }

            @Override
            public String getName() {
                return String.format("%s->%s", first, last);
            }

            @Override
            public Meta meta() {
                return Meta.empty();
            }
        };
    }
}
