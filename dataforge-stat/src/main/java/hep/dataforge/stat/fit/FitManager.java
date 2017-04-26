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

import hep.dataforge.actions.Action;
import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.context.PluginDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.TargetNotProvidedException;
import hep.dataforge.io.FittingIOUtils;
import hep.dataforge.io.reports.Loggable;
import hep.dataforge.meta.Meta;
import hep.dataforge.providers.Path;
import hep.dataforge.providers.Provider;
import hep.dataforge.stat.models.Model;
import hep.dataforge.stat.models.ModelManager;
import hep.dataforge.stat.models.XYModel;
import hep.dataforge.tables.Table;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author Alexander Nozik
 */
@PluginDef(group = "hep.dataforge", name = "fitting", description = "Basic dataforge fitting plugin")
public class FitManager extends BasicPlugin implements Provider {

    public static final String FIT_ENGINE_PROVIDER_KEY = "engine";
    private ModelManager modelManager;
    private HashMap<String, FitEngine> engineList = new HashMap<>();

    public FitManager() {
        addEngine("QOW", new QOWFitEngine());
        addEngine("CM", new CMFitEngine());
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        modelManager = new ModelManager(context);
    }

    @Override
    public Optional<?> provide(Path path) {
        switch (path.target()) {
            case FIT_ENGINE_PROVIDER_KEY:
                return Optional.ofNullable(engineList.get(path.nameString()));
            case Action.ACTION_PROVIDER_KEY:
                if(path.nameString().equals(FitAction.FIT_ACTION_NAME)){
                    return Optional.of(new FitAction());
                } else {
                    return Optional.empty();
                }
            default:
                throw new TargetNotProvidedException(path.target());
        }
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
        } else if (engineList.containsKey(name)) {
            return engineList.get(name);
        } else {
            throw new NameNotFoundException(name);
        }
    }

    /**
     * Add new fit engine to manager
     * @param name
     * @param ef
     */
    public void addEngine(String name, FitEngine ef) {
        engineList.put(name.toUpperCase(), ef);
    }

    public Model buildModel(String name) {
        return getModelManager().buildModel(name);
    }

    public Model buildModel(Meta modelAnnotation) {
        return getModelManager().buildModel(modelAnnotation);
    }

    public FitState buildState(Table data, String modelName, ParamSet pars) {
        Model model = getModelManager().buildModel(modelName);
        return new FitState(data, model, pars);
    }

    public FitState buildState(Table data, Meta modelAnnotation, ParamSet pars) {
        Model model = getModelManager().buildModel(modelAnnotation);
        return new FitState(data, model, pars);
    }

    public ModelManager getModelManager() {
        if (modelManager == null) {
            throw new RuntimeException("Fit manager not attached to context");
        }
        return modelManager;
    }

//    public FitResult runDefaultEngineTask(FitState state, String taskName, Loggable log, String... freePars) {
//        FitStage task = new FitStage(QOWFitEngine.QOW_ENGINE_NAME, taskName, freePars);
//        return runStage(state, task, log);
//    }

    public FitResult runDefaultStage(FitState state, Loggable log, String... freePars) {
        FitStage task = new FitStage(QOWFitEngine.QOW_ENGINE_NAME, FitStage.TASK_RUN, freePars);
        return runStage(state, task, log);
    }

    public FitResult runDefaultStage(FitState state, String... freePars) {
        return runDefaultStage(state, getContext(), freePars);
    }

    public FitResult runStage(FitState state, String engineName, String taskName, Loggable log, String... freePars) {
        FitStage task = new FitStage(engineName, taskName, freePars);
        return runStage(state, task, log);
    }

    public FitResult runStage(FitState state, String engineName, String taskName, String... freePars) {
        return runStage(state, engineName, taskName, getContext(), freePars);
    }

    public FitResult runStage(FitState state, FitStage task, Loggable log) {
        return runStage(state, task, Global.out(), log);
    }

    public FitResult runStage(FitState state, FitStage task, PrintWriter writer, Loggable log) {
        if (log == null) {
            log = getContext();
        }

        FitEngine engine = buildEngine(task.getEngineName());
        if (state == null) {
            throw new IllegalArgumentException("The fit state is not defined");
        }

        FitResult newState;

        switch (task.getName()) {
            //Тут идет обработка задач общих для всех движков
            case "print":
                state.printState(writer);
                return new FitResult(state, FitResult.emptyTask("print"));
            case "residuals":
                writer.printf("%n***RESIDUALS***%n");
                if (state.getModel() instanceof XYModel) {
                    FittingIOUtils.printSpectrumResiduals(writer, (XYModel) state.getModel(), state.getDataSet(), state.getParameters());
                } else {
                    FittingIOUtils.printResiduals(writer, state);
                }
                return new FitResult(state, FitResult.emptyTask("residuals"));
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
