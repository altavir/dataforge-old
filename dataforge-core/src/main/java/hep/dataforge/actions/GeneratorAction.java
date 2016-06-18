/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.actions;

import hep.dataforge.context.Context;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.io.reports.Report;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.meta.Meta;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.util.Pair;

/**
 * An action that does not take any input data, only generates output. Each output token is generated separately.
 *
 * @author Alexander Nozik
 */
public abstract class GeneratorAction<R> extends GenericAction<Void, R> {

    @Override
    public DataNode<R> run(Context context, DataNode<Void> data, Meta actionMeta) {
        Report log = new Report(getName(), context);
        Map<String, Pair<Meta, Supplier<R>>> generated = generate(context, actionMeta, log);
        Map<String, Data<R>> resultMap = new HashMap<>();
        generated.forEach((String s, Pair<Meta, Supplier<R>> pair) -> {
            resultMap.put(s, new ActionResult<>(getOutputType(), log,
                    postProcess(context, s, pair.getValue()), pair.getKey()));
        });
        return wrap(resultNodeName(), actionMeta, resultMap);
    }

    protected abstract Map<String, Pair<Meta, Supplier<R>>> generate(Context context, Meta meta, Reportable log);

    protected String resultNodeName() {
        return "";
    }

    @Override
    public Class getInputType() {
        return Void.class;
    }

}
