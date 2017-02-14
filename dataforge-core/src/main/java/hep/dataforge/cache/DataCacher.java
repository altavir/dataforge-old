package hep.dataforge.cache;

import hep.dataforge.data.Data;
import hep.dataforge.goals.AbstractGoal;
import hep.dataforge.goals.Goal;
import hep.dataforge.workspace.identity.Identity;

import javax.cache.Cache;
import java.util.stream.Stream;

/**
 * Created by darksnake on 10-Feb-17.
 */
public class DataCacher<T> {
    private Cache<Identity, T> cache;


    protected boolean canCache(Data<T> data) {
        return true;
    }

    public Data<T> cache(Data<T> data, Identity id) {
        if (!canCache(data)) {
            return data;
        } else {
            Goal<T> cachedGoal = new AbstractGoal<T>() {
                @Override
                protected T compute() throws Exception {
                    if (getCache().containsKey(id)) {
                        return getCache().get(id);
                    } else {
                        T res = data.get();
                        getCache().put(id, res);
                        return res;
                    }

                }

                @Override
                public Stream<Goal> dependencies() {
                    if (getCache().containsKey(id)) {
                        return Stream.empty();
                    } else {
                        return Stream.of(data.getGoal());
                    }
                }
            };
            return new Data<T>(cachedGoal, data.type(), data.meta());
        }
    }

    public Cache<Identity, T> getCache() {
        return cache;
    }

    public void invalidate(){
        getCache().removeAll();
    }

    public void invalidate(Identity id){
        getCache().remove(id);
    }

}
