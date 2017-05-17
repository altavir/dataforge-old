/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.api;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;

import java.util.function.Function;
import java.util.stream.Stream;

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
    Stream<T> query(Meta query) throws StorageException;


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
                .query(query)
                .map((it) -> transformation.apply(it));
    }

}
