/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.computation.Goal;
import hep.dataforge.computation.StaticGoal;

/**
 *
 * @author Alexander Nozik
 */
public class NamedData<T> extends Data<T> implements Named {

    public static <T> NamedData<T> buildStatic(String name, T content, Meta meta) {
        return new NamedData(name, new StaticGoal(content), meta, content.getClass());
    }

    private final String name;

    public NamedData(String name, Goal<T> goal, Meta meta, Class<T> type) {
        super(goal, meta, type);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
