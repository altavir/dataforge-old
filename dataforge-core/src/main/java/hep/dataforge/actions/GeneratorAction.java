/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.actions;

import hep.dataforge.context.Context;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.goals.GeneratorGoal;
import hep.dataforge.goals.Goal;
import hep.dataforge.io.history.Chronicle;
import hep.dataforge.meta.Meta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * An action that does not take any input data, only generates output. Each
 * output token is generated separately.
 *
 * @author Alexander Nozik
 */
public abstract class GeneratorAction<R> extends GenericAction<Void, R> {

    @Override
    public DataNode<R> run(Context context, DataNode<? extends Void> data, Meta actionMeta) {
        Chronicle log = context.getChronicle(getName());
        Map<String, Data<R>> resultMap = new ConcurrentHashMap<>();
        //TODO add optional parallelization here
        nameStream().forEach(name -> {
            Goal<R> goal = new GeneratorGoal<>(executor(context, actionMeta), () -> generateData(name));
            resultMap.put(name, new ActionResult<>(log, goal, generateMeta(name), getOutputType()));
        });
        return wrap(resultNodeName(), actionMeta, resultMap);
    }

    protected abstract Stream<String> nameStream();

    protected abstract Meta generateMeta(String name);

    protected abstract R generateData(String name);

    protected String resultNodeName() {
        return "";
    }

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

}
