/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.cache;

import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;
import hep.dataforge.workspace.identity.Identity;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage for cached data. Data is cached on first call and then restored on
 * subsequent calls without calculation.
 *
 * @author Alexander Nozik
 */
public abstract class DataCache {

    /**
     * Wrap data in cacher, which stores it on first call and restores on
     * subsequent calls
     *
     * @param <T>
     * @param data
     * @return
     */
    public <T> Data<T> cacheData(Data<T> data, Identity id) {
        return new CachedData<>(data, id);
    }

    /**
     * Create new node each element of which is cached.
     *
     * @param <T>
     * @param node
     * @param identityFactory
     * @return
     */
    public <T> DataTree<T> cacheNode(DataNode<T> node, BiFunction<String, Data<? extends T>, Identity> identityFactory) {
        DataTree.Builder<T> builder = new DataTree.Builder<>(node);
        node.dataStream().forEach(pair -> {
            Identity id = identityFactory.apply(pair.getKey(), pair.getValue());
            builder.putData(pair.getKey(), cacheData(pair.getValue(), id), true);
        });
        return builder.build();
    }

    public <T> DataTree<T> cacheNode(DataNode<T> node, Identity identity) {
        return cacheNode(node, (name, data) -> identity.and(name));
    }

    protected abstract <T> T restore(Identity id) throws DataCacheException;

    protected abstract <T> T store(Identity id, T data);

    protected abstract boolean contains(Identity id);

    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    protected class CachedData<T> implements Data<T> {

        private final Data<T> theData;
        private final Identity id;

        public CachedData(Data<T> theData, Identity id) {
            this.theData = theData;
            this.id = id;
        }

        @Override
        public CompletableFuture<T> get() {
            if (contains(id)) {
                try {
                    getLogger().debug("Restoring cached data with id '{}'", id.toString());
                    return CompletableFuture.completedFuture(DataCache.this.<T>restore(id));
                } catch (DataCacheException ex) {
                    getLogger().error("Failed to restore data with id '{}' from cache", id.toString());
                    return theData.get();
                }
            } else {
                return theData.get().thenApplyAsync(result -> store(id, result));
            }
        }

        @Override
        public T getNow() {
            if (contains(id)) {
                try {
                    getLogger().debug("Restoring cached data with id '{}'", id.toString());
                    return DataCache.this.<T>restore(id);
                } catch (DataCacheException ex) {
                    getLogger().error("Failed to restore data with id '{}' from cache", id.toString());
                    return theData.getNow();
                }
            } else {
                return get().join();
            }
        }

        @Override
        public Class<? super T> dataType() {
            return theData.dataType();
        }

        @Override
        public Meta meta() {
            return theData.meta();
        }

    }
}
