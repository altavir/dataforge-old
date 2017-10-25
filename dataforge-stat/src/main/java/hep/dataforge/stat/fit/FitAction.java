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
package hep.dataforge.stat.fit;

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.context.Context;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.io.history.Chronicle;
import hep.dataforge.meta.Laminate;
import hep.dataforge.tables.Table;

import java.io.OutputStream;
import java.io.PrintWriter;

import static hep.dataforge.values.ValueType.BOOLEAN;

/**
 * <p>
 * FitAction class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@TypedActionDef(name = "fit", inputType = Table.class, outputType = FitResult.class, info = "Fit dataset with previously stored model.")
@ValueDef(name = "printLog", type = {BOOLEAN}, def = "true", info = "Append log to the fit report")
@ValueDef(name = "model", info = "Could be uses instead of 'model' element in case of non-parametric models")
@NodeDef(name = "model",
        required = true, info = "The model against which fit should be made",
        from = "method::hep.dataforge.stat.models.ModelManager.getModel")
@NodeDef(name = "params", required = true,
        info = "Initial fit parameter set. Both parameters from action annotation and parameters from data annotation are used. "
                + "The merging of parameters is made supposing the annotation of data is main and annotation of action is secondary.",
        from = "method::hep.dataforge.stat.fit.ParamSet.fromMeta")
@NodeDef(name = "stage", multiple = true, info = "Fit stages")
public class FitAction extends OneToOneAction<Table, FitResult> {

    public static final String FIT_ACTION_NAME = "fit";

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    protected FitResult execute(Context context, String name, Table input, Laminate meta) {
        OutputStream output = context.io().out(getName(), name);
        PrintWriter writer = new PrintWriter(output);
        writer.printf("%n*** META ***%n");
        writer.println(meta.toString());
        writer.flush();

        Chronicle log = getLog(context, name);
        FitResult res = new FitHelper(context).fit(input, meta)
                .setListenerStream(output)
                .report(log)
                .run();

        if (meta.getBoolean("printLog", true)) {
            log.print(writer);
        }

        return res;
    }

//    @Override
//    protected void afterAction(Context context, String name, FitResult res, Laminate meta) {
//        super.afterAction(context, name, res, meta);
////        context.getChronicle(name).print(new PrintWriter(buildActionOutput(context, name)));
//    }
}
