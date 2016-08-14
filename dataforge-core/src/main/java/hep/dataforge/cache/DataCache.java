/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.cache;

import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.NamedData;
import hep.dataforge.workspace.identity.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

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
        if (contains(id)) {
            try {
                getLogger().debug("Restoring cached data with id '{}'", id.toString());
                data.getGoal().complete(restore(id));
            } catch (DataCacheException ex) {
                getLogger().error("Failed to restore data with id '{}' from cache", id.toString());
            }
        } else {
            data.getGoal().onComplete((res, ex) -> {
                if (res != null) {
                    getLogger().debug("Caching data with id '{}'", id.toString());
                    store(id, res);
                }
            });
        }
        return data;
//        
//        Goal<T> cachedGoal = new CachedGoal<>(data.getGoal(),id);
//        return new Data<>(cachedGoal,data.meta(),data.dataType());
    }

    /**
     * Create new node each element of which is cached.
     *
     * @param <T>
     * @param node
     * @param identityFactory
     * @return
     */
    public <T> DataNode<T> cacheNode(DataNode<T> node, Function<NamedData<? extends T>, Identity> identityFactory) {
        node.forEach(data -> cacheData(data, identityFactory.apply(data)));
        return node;
    }

    public <T> DataNode<T> cacheNode(DataNode<T> node, Identity identity) {
        return cacheNode(node, data -> identity.and(data.getName()));
    }

    /**
     * Restore value with given identity from cache
     *
     * @param <T>
     * @param id
     * @return
     * @throws DataCacheException
     */
    protected abstract <T> T restore(Identity id) throws DataCacheException;

    /**
     * Save given value with given identity to cache
     *
     * @param <T>
     * @param id
     * @param data
     * @return
     */
    protected abstract <T> T store(Identity id, T data);

    /**
     * Invalidate cache element with given id
     *
     * @param id
     */
    protected abstract void invalidate(Identity id);

    /**
     * Check if cache contains given id
     *
     * @param id
     * @return
     */
    protected abstract boolean contains(Identity id);

    /**
     * logger for this cache
     *
     * @return
     */
    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

//    protected class CachedGoal<T> implements Goal<T> {
//
//        private final Goal<T> goal;
//        private final Identity id;
//        private final CompletableFuture<T> future;
//
//        public CachedGoal(Goal<T> goal, Identity id) {
//            this.goal = goal;
//            this.id = id;
//            future = goal.result().thenApplyAsync(res -> {
//                store(id, res);
//                return res;
//            });
//        }
//
//        @Override
//        public Stream<Goal> dependencies() {
//            if (contains(id)) {
//                return Stream.empty();
//            } else {
//                return goal.dependencies();
//            }
//        }
//
//        @Override
//        public void start() {
//            if (contains(id)) {
//                try {
//                    getLogger().debug("Restoring cached data with id '{}'", id.toString());
//                    future.complete(restore(id));
//                } catch (DataCacheException ex) {
//                    getLogger().error("Failed to restore data with id '{}' from cache", id.toString());
//                    goal.start();
//                }
//            } else {
//                goal.start();
//            }
//        }
//
//        @Override
//        public CompletableFuture<T> result() {
//            return future;
//        }
//
//    }
}
