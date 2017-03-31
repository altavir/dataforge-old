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
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.stat.models.Model;
import hep.dataforge.stat.models.ModelManager;
import hep.dataforge.tables.Table;
import hep.dataforge.utils.Misc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * FitAction class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@TypedActionDef(name = "fit", inputType = Table.class, outputType = FitState.class, info = "Fit dataset with previously stored model.")
@ValueDef(name = "model", info = "Could be uses instead of 'model' element in case of non-parametric models")
@NodeDef(name = "model",
        required = true, info = "The model against which fit should be made",
        target = "method::hep.dataforge.stat.models.ModelManager.buildModel")
@NodeDef(name = "params", required = true,
        info = "Initial fit parameter set. Both parameters from action annotation and parameters from data annotation are used. "
                + "The merging of parameters is made supposing the annotation of data is main and annotation of action is secondary.",
        target = "method::hep.dataforge.stat.fit.ParamSet.fromMeta")
@NodeDef(name = "stage", multiple = true, info = "Fit stages")
public class FitAction extends OneToOneAction<Table, FitState> {

    public static final String FIT_ACTION_NAME = "fit";

    public static final String STAGE_KEY = "stage";

    public static final String MODEL_KEY = "model";

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    protected FitState execute(Context context, String name, Table input, Laminate meta) {
        FitManager fm;
        if (context.provides("fitting")) {
            fm = context.provide("fitting", FitManager.class);
        } else {
            fm = new FitManager(context);
        }

        List<FitStage> stages = buildStageList(meta);

//        boolean printresult = meta().getBoolean("printresult", true);
        if (stages.isEmpty()) {
            throw new ContentException("No fit tasks defined");
        }

        FitState res = buildInitialState(context, meta, input, fm);
        PrintWriter writer = new PrintWriter(buildActionOutput(context, name));

        for (FitStage task : stages) {
            Misc.checkThread();// check if action is cacneled
            res = fm.runStage(res, task, writer, getReport(context, name));
        }
        getReport(context, name).print(writer);
        return res;
    }

    private FitState buildInitialState(Context context, Laminate meta, Table input, FitManager fm) {
        Model model;

        ModelManager mm = fm.getModelManager();

        if (meta.hasMeta(MODEL_KEY)) {
            model = mm.buildModel(meta.getMeta(MODEL_KEY));
        } else {
            model = mm.buildModel(meta.getString(MODEL_KEY));
        }

        ParamSet params;

        //updating parameters for each laminate 
        params = new ParamSet();
        meta.layersInverse().stream().forEach((layer) -> {
            params.updateFrom(ParamSet.fromMeta(layer));
        });

        return new FitState(input, model, params);

    }

    private List<FitStage> buildStageList(Meta meta) {
        List<FitStage> list = new ArrayList<>();
        if (meta.hasMeta(STAGE_KEY)) { // Пробуем взять набор задач из аннотации данных или аннотации действия
            meta.getMetaList(STAGE_KEY).stream().forEach((an) -> {
                list.add(new FitStage(an));
            });
        } else { // если и там нет, то считаем что имеется всего одна задача и она зашифрована в а аннотациях
            list.add(new FitStage(meta));

        }

        list.add(new FitStage("print"));
        list.add(new FitStage("residuals"));

        return list;
    }

}
