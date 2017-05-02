package hep.dataforge.stat.fit;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.io.markup.MarkupBuilder;
import hep.dataforge.io.reports.Loggable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.stat.models.Model;
import hep.dataforge.stat.models.XYModel;
import hep.dataforge.stat.parametric.ParametricFunction;
import hep.dataforge.tables.NavigablePointSource;
import hep.dataforge.tables.XYAdapter;
import hep.dataforge.utils.Misc;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static hep.dataforge.stat.fit.FitStage.STAGE_KEY;

/**
 * A helper class to run simple fits without building context and generating meta
 * Created by darksnake on 14-Apr-17.
 */
public class FitHelper {
    public static final String MODEL_KEY = "model";

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

    private List<FitStage> buildStageList(Meta meta) {
        if (meta.hasMeta(STAGE_KEY)) {
            return meta.getMetaList(STAGE_KEY).stream().map(m -> new FitStage(m)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    /**
     * Meta as described in {@link FitAction}
     *
     * @param data
     * @param meta
     * @return
     */
    public FitBuilder fit(NavigablePointSource data, Meta meta) {
        return new FitBuilder(data).update(meta);
    }

    public FitBuilder fit(NavigablePointSource data) {
        return new FitBuilder(data);
    }

    public class FitBuilder {
        NavigablePointSource data;
        Model model;
        ParamSet startPars = new ParamSet();
        Loggable log = null;
        List<FitStage> stages = new ArrayList<>();

        public FitBuilder(@NotNull NavigablePointSource data) {
            this.data = data;
        }

        public FitBuilder update(Meta meta) {
            if (meta.hasMeta(MODEL_KEY)) {
                model(meta.getMeta(MODEL_KEY));
            } else if (meta.hasValue(MODEL_KEY)) {
                model(meta.getString(MODEL_KEY));
            }
            List<FitStage> stages = buildStageList(meta);
            if (!stages.isEmpty()) {
                allStages(stages);
            }
            params(meta);
            return this;
        }

        public FitBuilder report(Loggable report) {
            this.log = report;
            return this;
        }

        public FitBuilder report(String reportName) {
            this.log = getManager().getContext().getLog(reportName);
            return this;
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

        public FitBuilder params(Meta meta) {
            if (meta instanceof Laminate) {
                ParamSet set = new ParamSet();
                Laminate laminate = (Laminate) meta;
                laminate.layersInverse().stream().forEach((layer) -> {
                    set.updateFrom(ParamSet.fromMeta(layer));
                });
                return params(set);
            } else {
                return params(ParamSet.fromMeta(meta));
            }
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

        /**
         * Set all fit stages clearing old ones
         *
         * @param stages
         * @return
         */
        public FitBuilder allStages(List<FitStage> stages) {
            this.stages.clear();
            this.stages.addAll(stages);
            return this;
        }

        public FitBuilder showReult() {
            return stage(new FitStage("print"))
                    .stage(new FitStage("residuals"));
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

            MarkupBuilder report = new MarkupBuilder();

            FitState state = new FitState(data, model, startPars);
            if (stages.isEmpty()) {
                state = manager.runDefaultStage(state, log);
            } else {
                for (FitStage stage : stages) {
                    Misc.checkThread();
                    try {
                        state = manager.runStage(state, stage, log);
                    } catch (Exception ex) {
                        FitResult res = new FitResult(state, stage);
                        res.setValid(false);
                        state = res;
                    }
                }
            }

            return FitResult.class.cast(state);
        }
    }
}

/*
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
 */