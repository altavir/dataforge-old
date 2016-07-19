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
package hep.dataforge.datafitter;

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.datafitter.models.Model;
import hep.dataforge.datafitter.models.ModelManager;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.tables.Table;
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
        target = "method::hep.dataforge.datafitter.models.ModelManager.buildModel")
@NodeDef(name = "params", required = true,
        info = "Initial fit parameter set. Both parameters from action annotation and parameters from data annotation are used. "
        + "The merging of parameters is made supposing the annotation of data is main and annotation of action is secondary.",
        target = "method::hep.dataforge.datafitter.ParamSet.fromAnnotation")
@NodeDef(name = "task", multiple = true, info = "Fit tasks")
public class FitAction extends OneToOneAction<Table, FitState> {

    public static final String FIT_ACTION_NAME = "fit";

    public static final String TASK_PATH = "task";

    public static final String MODEL_PATH = "model";

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    protected FitState execute(Reportable log, String name, Laminate meta, Table input) {
        FitManager fm;
        if (getContext().provides("fitting")) {
            fm = getContext().provide("fitting", FitPlugin.class).getFitManager();
        } else {
            fm = new FitManager(getContext());
        }

        List<FitTask> tasks = buildTaskList(meta);

//        boolean printresult = meta().getBoolean("printresult", true);
        if (tasks.isEmpty()) {
            throw new ContentException("No fit tasks defined");
        }

        FitState res = buildInitialState(meta, input, fm);
        PrintWriter writer = new PrintWriter(buildActionOutput(name));

        for (FitTask task : tasks) {
            res = fm.runTask(res, task, writer, log);
        }
        log.getReport().print(writer);
        return res;
    }

    private FitState buildInitialState(Laminate meta, Table input, FitManager fm) {
        Model model;

        ModelManager mm = fm.getModelManager();

        if (meta.hasNode(MODEL_PATH)) {
            model = mm.buildModel(getContext(), meta.getNode(MODEL_PATH));
        } else {
            model = mm.buildModel(getContext(), meta.getString(MODEL_PATH));
        }

        ParamSet params;

        //updating parameters for each laminate 
        params = new ParamSet();
        meta.layersInverse().stream().forEach((layer) -> {
            params.updateFrom(ParamSet.fromAnnotation(layer));
        });

        return new FitState(input, model, params);

    }

    private List<FitTask> buildTaskList(Meta annotation) {
        List<FitTask> list = new ArrayList<>();
        if (annotation.hasNode(TASK_PATH)) { // Пробуем взять набор задач из аннотации данных или аннотации действия
            annotation.getNodes(TASK_PATH).stream().forEach((an) -> {
                list.add(new FitTask(an));
            });
        } else { // если и там нет, то считаем что имеется всего одна задача и она зашифрована в а аннотациях
            list.add(new FitTask(annotation));

        }

        list.add(new FitTask("print"));
        list.add(new FitTask("residuals"));

        return list;
    }

}
