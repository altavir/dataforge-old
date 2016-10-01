/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.api;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Alexander Nozik
 */
public interface Index<T> {

    /**
     * Return a stream of suppliers of objects corresponding to query
     *
     * @param query
     * @return
     * @throws StorageException
     */
    List<Supplier<T>> query(Meta query) throws StorageException;


    /**
     * Create new index that uses a transformation for each of this index result items.
     *
     * @param <R>
     * @param transformation
     * @return
     */
    default <R> Index<R> transform(Function<T, R> transformation) {
        final Index<T> theIndex = this;
        return (Meta query) -> theIndex
                .query(query).stream()
                .map((Supplier<T> t) -> (Supplier<R>) () -> transformation.apply(t.get()))
                .collect(Collectors.toList());
    }

}
