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
import hep.dataforge.context.*;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.history.History;
import hep.dataforge.meta.Meta;
import hep.dataforge.providers.Path;
import hep.dataforge.stat.models.Model;
import hep.dataforge.stat.models.ModelManager;
import hep.dataforge.tables.Table;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author Alexander Nozik
 */
@PluginDef(group = "hep.dataforge", name = "fitting", dependsOn = "hep.dataforge:models", info = "Basic dataforge fitting plugin")
public class FitManager extends BasicPlugin {

    public static final String FIT_ENGINE_TARGET = "fitEngine";
    private HashMap<String, FitEngine> engineList = new HashMap<>();
    private transient ModelManager modelManager;

    public FitManager() {
        addEngine("QOW", new QOWFitEngine());
        addEngine("CM", new CMFitEngine());
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        modelManager = context.get(ModelManager.class);
    }

    @Override
    public Optional<?> provide(Path path) {
        switch (path.getTarget()) {
            case FIT_ENGINE_TARGET:
                return Optional.ofNullable(engineList.get(path.nameString()));
            case Action.ACTION_TARGET:
                if (path.nameString().equals(FitAction.FIT_ACTION_NAME)) {
                    return Optional.of(new FitAction());
                } else {
                    return Optional.empty();
                }
            default:
                return Optional.empty();
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

    private ModelManager getModelManager() {
        if (modelManager == null) {
            throw new IllegalStateException("Fit plugin or model manage not attached to context");
        }
        return modelManager;
    }

    /**
     * Add new fit engine to manager
     *
     * @param name
     * @param ef
     */
    public void addEngine(String name, FitEngine ef) {
        engineList.put(name.toUpperCase(), ef);
    }

    public Model buildModel(String name) {
        return getModelManager().getModel(name).orElseThrow(() -> new RuntimeException("Model not defined"));
    }

    public Model buildModel(Meta meta) {
        return getModelManager().getModel(meta).orElseThrow(() -> new RuntimeException("Model not defined"));
    }

    public FitState buildState(Table data, String modelName, ParamSet pars) {
        Model model = buildModel(modelName);
        return new FitState(data, model, pars);
    }

    public FitState buildState(Table data, Meta meta, ParamSet pars) {
        Model model = buildModel(meta);
        return new FitState(data, model, pars);
    }

    public FitResult runDefaultStage(FitState state, String... freePars) {
        return runDefaultStage(state, getContext(), freePars);
    }

    public FitResult runDefaultStage(FitState state, History log, String... freePars) {
        FitStage task = new FitStage(QOWFitEngine.QOW_ENGINE_NAME, FitStage.TASK_RUN, freePars);
        return runStage(state, task, log);
    }

    public FitResult runStage(FitState state, String engineName, String taskName, String... freePars) {
        FitStage task = new FitStage(engineName, taskName, freePars);
        return runStage(state, task, getContext());
    }

    public FitResult runStage(FitState state, FitStage task, History log) {
        FitEngine engine = buildEngine(task.getEngineName());
        if (state == null) {
            throw new IllegalArgumentException("The fit state is not defined");
        }

        log.report("Starting fit task {}", task.toString());
        FitResult newState = engine.run(state, task, log);

        if (!newState.isValid()) {
            log.reportError("The result of the task is not a valid state");
        }
        return newState;
    }

    public static class Factory implements PluginFactory {
        @Override
        public Plugin build(Meta meta) {
            return new FitManager();
        }

        @Override
        public Class<? extends Plugin> type() {
            return FitManager.class;
        }
    }
}
