/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.actions;

import hep.dataforge.computation.GeneratorGoal;
import hep.dataforge.computation.Goal;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.io.reports.Log;
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
    public DataNode<R> run(DataNode<? extends Void> data, Meta actionMeta) {
        Log log = new Log(getName(), getContext());
        Map<String, Data<R>> resultMap = new ConcurrentHashMap<>();
        //TODO add optional parallelization here
        nameStream().forEach(name -> {
            Goal<R> goal = new GeneratorGoal<>(() -> generateData(name), executor(actionMeta));
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
    public Class getInputType() {
        return Void.class;
    }

}
