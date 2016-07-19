/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A simple lazy data source
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class LazyData<T> implements Data<T> {

    private final CompletableFuture<T> future;
    private final Class<? super T> type;

    public LazyData(Class<? super T> type, Supplier<T> sup) {
        this.type = type;
        this.future = CompletableFuture.<T>supplyAsync(sup);
    }
    
    public LazyData(Class<? super T> type, CompletableFuture<T> future) {
        this.future = future;
        this.type = type;
    }

    @Override
    public CompletableFuture<T> get() {
        return future;
    }

    @Override
    public Class<? super T> dataType() {
        return type;
    }

}
