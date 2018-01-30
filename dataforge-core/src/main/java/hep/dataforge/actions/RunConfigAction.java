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
import hep.dataforge.data.DataNode;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Meta;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;

@TypedActionDef(name = "run", info = "Run action with given configuration")
public class RunConfigAction extends GenericAction {

    /**
     * {@inheritDoc}
     *
     * @param input
     * @return
     */
    @Override
    public DataNode run(Context context, DataNode input, Meta actionMeta) {
        Meta cfg;

        Meta meta = inputMeta(context, input.getMeta(), actionMeta);

        String contextName = meta.getString("contextName", getName());
        Context ac = Context.Companion.builder(contextName,context).build();
        if (meta.hasValue("configFile")) {
            Path cfgFile = context.getIo().getFile(meta.getString("configFile"));
            try {
                cfg = MetaFileReader.instance().read(ac, cfgFile, null);
            } catch (IOException | ParseException ex) {
                throw new ContentException("Can't read config file", ex);
            }
        } else {
            cfg = actionMeta;
        }
        return ActionUtils.runConfig(ac, cfg);
    }

}
