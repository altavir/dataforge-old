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

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;
import hep.dataforge.context.Global;
import hep.dataforge.io.FittingIOUtils;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.meta.Meta;
import hep.dataforge.stat.models.Model;
import hep.dataforge.stat.models.ModelManager;
import hep.dataforge.stat.models.XYModel;
import hep.dataforge.tables.Table;

import java.io.PrintWriter;

/**
 * <p>
 * FitTaskManager class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class FitManager implements Encapsulated {


    private final Context context;

    protected final ModelManager modelManager;

    public FitManager() {
        this.context = Global.instance();
        modelManager = new ModelManager();
    }

    public FitManager(Context context) {
        this.context = context;
        modelManager = new ModelManager();
    }

    public FitManager(Context context, ModelManager modelManager) {
        this.context = context;
        this.modelManager = modelManager;
    }

    public FitEngine buildEngine(String name) {
        if (name == null || name.isEmpty()) {
            getContext().report("The fitting engine name is not defined. Using QOW engine by default");
            return new QOWFitEngine();
        }

        if (name.equalsIgnoreCase(QOWFitEngine.QOW_ENGINE_NAME)) {
            return new QOWFitEngine();
        } else if (name.equalsIgnoreCase(CMFitEngine.CM_ENGINE_NAME)) {
            return new CMFitEngine();
        } else {
            return FitEngineBuilder.buildEngine(name);
        }
    }

    public Model buildModel(String name) {
        return getModelManager().buildModel(getContext(), name);
    }

    public Model buildModel(Meta modelAnnotation) {
        return getModelManager().buildModel(getContext(), modelAnnotation);
    }

    public FitState buildState(Table data, String modelName, ParamSet pars) {
        Model model = getModelManager().buildModel(getContext(), modelName);
        return new FitState(data, model, pars);
    }

    public FitState buildState(Table data, Meta modelAnnotation, ParamSet pars) {
        Model model = getModelManager().buildModel(getContext(), modelAnnotation);
        return new FitState(data, model, pars);
    }

    @Override
    public Context getContext() {
        return context;
    }

    public ModelManager getModelManager() {
        return modelManager;
    }

    public FitTaskResult runDefaultEngineTask(FitState state, String taskName, Reportable log, String... freePars) {
        FitStage task = new FitStage(QOWFitEngine.QOW_ENGINE_NAME, taskName, freePars);
        return runTask(state, task, log);
    }

    public FitTaskResult runDefaultTask(FitState state, Reportable log, String... freePars) {
        FitStage task = new FitStage(QOWFitEngine.QOW_ENGINE_NAME, FitStage.TASK_RUN, freePars);
        return runTask(state, task, log);
    }

    public FitTaskResult runDefaultTask(FitState state, String... freePars) {
        return runDefaultTask(state, getContext(), freePars);
    }

    public FitTaskResult runTask(FitState state, String engineName, String taskName, Reportable log, String... freePars) {
        FitStage task = new FitStage(engineName, taskName, freePars);
        return runTask(state, task, log);
    }

    public FitTaskResult runTask(FitState state, String engineName, String taskName, String... freePars) {
        return runTask(state, engineName, taskName, getContext(), freePars);
    }

    public FitTaskResult runTask(FitState state, FitStage task, Reportable log) {
        return runTask(state, task, Global.out(), log);
    }

    public FitTaskResult runTask(FitState state, FitStage task, PrintWriter writer, Reportable log) {
        if(log == null){
            log = getContext();
        }
        
        FitEngine engine = buildEngine(task.getEngineName());
        if (state == null) {
            throw new IllegalArgumentException("The fit state is not defined");
        }

        FitTaskResult newState;

        switch (task.getName()) {
            //Тут идет обработка задач общих для всех движков
            case "print":
                state.print(writer);
                return new FitTaskResult(state, FitTaskResult.emptyTask("print"));
            case "residuals":
                writer.printf("%n***RESIDUALS***%n");
                if (state.getModel() instanceof XYModel) {
                    FittingIOUtils.printSpectrumResiduals(writer, (XYModel) state.getModel(), state.getDataSet(), state.getParameters());
                } else {
                    FittingIOUtils.printResiduals(writer, state);
                }
                return new FitTaskResult(state, FitTaskResult.emptyTask("residuals"));
            default:
                log.report("Starting task {}", task.toString());
                newState = engine.run(state, task, log);
        }

        if (!newState.isValid()) {
            log.reportError("The result of the task is not a valid state");
        }
        return newState;
    }
}
