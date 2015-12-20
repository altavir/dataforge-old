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

import hep.dataforge.meta.Meta;
import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.data.DataSet;
import hep.dataforge.datafitter.models.Model;
import hep.dataforge.datafitter.models.ModelManager;
import hep.dataforge.datafitter.models.XYModel;
import hep.dataforge.io.log.Logable;
import hep.dataforge.io.PrintNamed;
import java.io.PrintWriter;
import static hep.dataforge.io.PrintNamed.printResiduals;
import static hep.dataforge.io.PrintNamed.printResiduals;
import hep.dataforge.context.Encapsulated;
import static hep.dataforge.io.PrintNamed.printResiduals;
import static hep.dataforge.io.PrintNamed.printResiduals;

/**
 * <p>
 * FitTaskManager class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class FitManager implements Encapsulated {

    /**
     * <p>
     * buildState.</p>
     *
     * @param data a {@link hep.dataforge.data.DataSet} object.
     * @param model a {@link hep.dataforge.datafitter.models.Model} object.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public static FitState buildState(DataSet data, Model model, ParamSet pars) {
        String name;
        if (data.isAnonimous()) {
            name = "fitState";
        } else {
            name = data.getName();
        }
        return new FitState(name, null, data, model, pars);
    }

    private final Context context;

    /**
     *
     */
    protected final ModelManager modelManager;

    /**
     * <p>
     * Constructor for FitManager.</p>
     */
    public FitManager() {
        this.context = GlobalContext.instance();
        modelManager = new ModelManager();
    }

    /**
     * <p>
     * Constructor for FitManager.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     */
    public FitManager(Context context) {
        this.context = context;
        modelManager = new ModelManager();
    }

    /**
     * <p>
     * Constructor for FitManager.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param modelManager a
     * {@link hep.dataforge.datafitter.models.ModelManager} object.
     */
    public FitManager(Context context, ModelManager modelManager) {
        this.context = context;
        this.modelManager = modelManager;
    }

//    public void addModel(String name, ParametricFactory<Model> mf) {
//        getModelManager().addModel(name, mf);
//    }
    /**
     * <p>
     * buildEngine.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.FitEngine} object.
     */
    public FitEngine buildEngine(String name) {
        if (name == null || name.isEmpty()) {
            getContext().log("The fitting engine name is not defined. Using QOW engine by default");
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

    /**
     * <p>
     * buildModel.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.models.Model} object.
     */
    public Model buildModel(String name) {
        return getModelManager().buildModel(getContext(), name);
    }

    /**
     * <p>
     * buildModel.</p>
     *
     * @param modelAnnotation a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.datafitter.models.Model} object.
     */
    public Model buildModel(Meta modelAnnotation) {
        return getModelManager().buildModel(getContext(), modelAnnotation);
    }

    /**
     * <p>
     * buildState.</p>
     *
     * @param data a {@link hep.dataforge.data.DataSet} object.
     * @param modelName a {@link java.lang.String} object.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public FitState buildState(DataSet data, String modelName, ParamSet pars) {
        Model model = getModelManager().buildModel(getContext(), modelName);
        return buildState(data, model, pars);
    }

    /**
     * <p>
     * buildState.</p>
     *
     * @param data a {@link hep.dataforge.data.DataSet} object.
     * @param modelAnnotation a {@link hep.dataforge.meta.Meta} object.
     * @param pars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public FitState buildState(DataSet data, Meta modelAnnotation, ParamSet pars) {
        Model model = getModelManager().buildModel(getContext(), modelAnnotation);
        return buildState(data, model, pars);
    }

    @Override
    public Context getContext() {
        return context;
    }

    /**
     * <p>
     * Getter for the field <code>modelManager</code>.</p>
     *
     * @return the modelManager
     */
    public ModelManager getModelManager() {
        return modelManager;
    }

    /**
     * <p>
     * runDefaultEngineTask.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param taskName a {@link java.lang.String} object.
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param freePars a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public FitTaskResult runDefaultEngineTask(FitState state, String taskName, Logable log, String... freePars) {
        FitTask task = new FitTask(QOWFitEngine.QOW_ENGINE_NAME, taskName, freePars);
        return runTask(state, task, log);
    }

    public FitTaskResult runDefaultTask(FitState state, Logable log, String... freePars) {
        FitTask task = new FitTask(QOWFitEngine.QOW_ENGINE_NAME, FitTask.TASK_RUN, freePars);
        return runTask(state, task, log);
    }

    public FitTaskResult runDefaultTask(FitState state, String... freePars) {
        return runDefaultTask(state, getContext(), freePars);
    }

    public FitTaskResult runTask(FitState state, String engineName, String taskName, Logable log, String... freePars) {
        FitTask task = new FitTask(engineName, taskName, freePars);
        return runTask(state, task, log);
    }

    /**
     * <p>
     * runTask.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param engineName a {@link java.lang.String} object.
     * @param taskName a {@link java.lang.String} object.
     * @param freePars a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public FitTaskResult runTask(FitState state, String engineName, String taskName, String... freePars) {
        return runTask(state, engineName, taskName, getContext(), freePars);
    }

    /**
     * <p>
     * runTask.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param task a {@link hep.dataforge.datafitter.FitTask} object.
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public FitTaskResult runTask(FitState state, FitTask task, Logable log) {
        return runTask(state, task, GlobalContext.out(), log);
    }

    /**
     * <p>
     * runTask.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param task a {@link hep.dataforge.datafitter.FitTask} object.
     * @param writer a {@link java.io.PrintWriter} object.
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @return a {@link hep.dataforge.datafitter.FitState} object.
     */
    public FitTaskResult runTask(FitState state, FitTask task, PrintWriter writer, Logable log) {
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
                    PrintNamed.printSpectrumResiduals(writer, (XYModel) state.getModel(), state.getDataSet(), state.getParameters());
                } else {
                    printResiduals(writer, state);
                }
                return new FitTaskResult(state, FitTaskResult.emptyTask("residuals"));
            default:
                log.log("Starting task {}", task.toString());
                newState = engine.run(state, task, log);
        }

        if (!newState.isValid()) {
            log.logError("The result of the task is not a valid state");
        }
        return newState;
    }
}
