/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.goals.Goal;
import hep.dataforge.goals.StaticGoal;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.AnonymousNotAlowed;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;

/**
 * A data with name
 *
 * @author Alexander Nozik
 */
@AnonymousNotAlowed
public class NamedData<T> extends Data<T> implements Named {

    @SuppressWarnings("unchecked")
    public static <T> NamedData<T> buildStatic(String name, T content, Meta meta) {
        return new NamedData<T>(name, new StaticGoal<T>(content), (Class<T>) content.getClass(), meta);
    }

    private final String name;

    public NamedData(String name, Goal<T> goal, Class<T> type, Meta meta) {
        super(goal, type, meta);
        if(name == null|| name.isEmpty()){
            throw new AnonymousNotAlowedException();
        }
        this.name = name;
    }

    /**
     * Wrap existing data using name and layers of external meta if it is available
     *
     * @param name
     * @param data
     * @param externalMeta
     * @param <T>
     * @return
     */
    public static <T> NamedData<T> wrap(String name, Data<T> data, Meta... externalMeta) {
        Laminate newMeta = new Laminate(data.meta()).withLayer(externalMeta);
        return new NamedData<T>(name, data.getGoal(), data.type(), newMeta);
    }

    public static <T> NamedData<T> wrap(Name name, Data<T> data, Laminate externalMeta) {
        Laminate newMeta = externalMeta.withFirstLayer(data.meta());
        return new NamedData<T>(name.toString(), data.getGoal(), data.type(), newMeta);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Return unnamed data corresponding to this named one
     *
     * @return
     */
    public Data<T> anonymize() {
        return new Data<T>(this.getGoal(), this.type(), this.meta());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> NamedData<R> cast(Class<R> type) {
        if (type.isAssignableFrom(type())) {
            return new NamedData<R>(name, (Goal<R>) getGoal(), type, meta());
        } else {
            throw new IllegalArgumentException("Invalid type to upcast data");
        }
    }
}
