package hep.dataforge.stat.fit;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.io.reports.Loggable;
import hep.dataforge.meta.Meta;
import hep.dataforge.stat.models.Model;
import hep.dataforge.stat.models.XYModel;
import hep.dataforge.stat.parametric.ParametricFunction;
import hep.dataforge.tables.RowProvider;
import hep.dataforge.tables.XYAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to run simple fits without building context and generating meta
 * Created by darksnake on 14-Apr-17.
 */
public class FitHelper {
    private FitManager manager;

    public FitHelper(Context context) {
        this.manager = context.pluginManager().getOrLoad(FitManager.class);
    }

    public FitHelper() {
        this(Global.getDefaultContext());
    }

    public FitManager getManager() {
        return manager;
    }

    public FitBuilder fit(RowProvider data) {
        return new FitBuilder(data);
    }

    public class FitBuilder {
        RowProvider data;
        Model model;
        ParamSet startPars = new ParamSet();
        Loggable log = null;
        List<FitStage> stages = new ArrayList<>();

        public FitBuilder(@NotNull RowProvider data) {
            this.data = data;
        }

        public FitBuilder model(String name) {
            this.model = manager.buildModel(name);
            return this;
        }

        public FitBuilder model(Meta meta) {
            this.model = manager.buildModel(meta);
            return this;
        }

        public FitBuilder function(ParametricFunction func, XYAdapter adapter) {
            this.model = new XYModel(func, adapter);
            return this;
        }

        public FitBuilder params(ParamSet params) {
            this.startPars.updateFrom(params);
            return this;
        }

        public FitBuilder param(String name, double value, double error) {
            this.startPars.setPar(name, value, error);
            return this;
        }


        public FitBuilder stage(FitStage stage) {
            stages.add(stage);
            return this;
        }

        public FitBuilder stage(String engineName, String taskName, String... freeParameters) {
            if (freeParameters.length == 0) {
                stages.add(new FitStage(engineName, taskName));
            } else {
                stages.add(new FitStage(engineName, taskName, freeParameters));
            }
            return this;
        }

        public FitResult run() {
            if (data == null) {
                throw new RuntimeException("Data not set");
            }

            if (model == null) {
                throw new RuntimeException("Model not set");
            }

            FitState state = new FitState(data, model, startPars);
            if (stages.isEmpty()) {
                state = manager.runDefaultStage(state, log);
            } else {
                for (FitStage stage : stages) {
                    state = manager.runStage(state, stage, log);
                }
            }
            return FitResult.class.cast(state);
        }
    }
}
