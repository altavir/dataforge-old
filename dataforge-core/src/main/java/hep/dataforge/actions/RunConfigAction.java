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
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Meta;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import hep.dataforge.data.DataNode;

@TypedActionDef(name = "run", description = "Run action with given configuration")
@ValueDef(name = "configFile", info = "The configuration file name and path")
@ValueDef(name = "contextName", info = "The name of the context in which the action will be run")
public class RunConfigAction extends GenericAction {

    public RunConfigAction(Context context, Meta annotation) {
        super(context, "run", annotation);
    }

    /**
     * {@inheritDoc}
     *
     * @param input
     * @return
     */
    @Override
    public DataNode run(DataNode input){
        Meta cfg;

        Meta meta = inputMeta(input.meta());

        String contextName = meta.getString("contextName", getName());
        Context ac = new Context(getContext(), contextName);
        if (meta.hasValue("configFile")) {
            File cfgFile = getContext().io().getFile(meta.getString("configFile"));
            try {
                cfg = MetaFileReader.instance().read(ac, cfgFile, null);
            } catch (IOException | ParseException ex) {
                throw new ContentException("Can't read config file", ex);
            }
        } else {
            cfg = meta();
        }
        return RunManager.executeAction(ac, cfg);
    }

}
