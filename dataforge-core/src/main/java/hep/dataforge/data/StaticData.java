/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.names.BaseMetaHolder;
import java.util.concurrent.CompletableFuture;

/**
 * Statically generated data. It still uses lazy interface to ensure following calculation steps are made on demand only.
 * @author Alexander Nozik
 */
public class StaticData<T> extends BaseMetaHolder implements Data<T> {

    private final T object;
    private final Class<T> type;

    public StaticData(T object) {
        if (object == null) {
            throw new IllegalArgumentException("Data in DataSource could not be null");
        }

        this.object = object;
        type = (Class<T>) object.getClass();
    }

    public StaticData(T object, Class<T> type) {
        if (object == null) {
            throw new IllegalArgumentException("Data in DataSource could not be null");
        }

        this.object = object;
        this.type = type;
    }

    @Override
    public boolean isValid() {
        return object != null;
    }

    @Override
    public T getNow() {
        return object;
    }

    @Override
    public CompletableFuture<T> get() {
        return CompletableFuture.supplyAsync(() -> object);
    }

    @Override
    public Class<T> dataType() {
        return type;
    }

}
