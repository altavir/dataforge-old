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

import hep.dataforge.content.NullContent;
import hep.dataforge.context.Context;
import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ActionExecutionException;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * <p>
 * RunConfigAction class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@TypedActionDef(name = "run", inputType = NullContent.class, description = "Run action with given configuration")
@ValueDef(name = "configFile", info = "The configuration file name and path")
@ValueDef(name = "contextName", info = "The name of the context in which the action will be run")
public class RunConfigAction extends GenericAction {

    /**
     * <p>
     * Constructor for RunConfigAction.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public RunConfigAction(Context context, Meta annotation) {
        super(context, "run", annotation);
    }

    @Override
    protected List execute(Logable log, Meta packAnnotation, DependencySet input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * {@inheritDoc}
     *
     * @param input
     * @return
     */
    @Override
    public ActionResult run(DependencySet input) throws ActionExecutionException {
        Meta cfg;

        Meta meta = readMeta(input.meta());

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
