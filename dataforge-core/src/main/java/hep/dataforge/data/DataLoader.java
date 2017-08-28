package hep.dataforge.data;

import hep.dataforge.context.Context;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Named;
import hep.dataforge.utils.ContextMetaFactory;

/**
 * A common interface for data providers
 * Created by darksnake on 02-Feb-17.
 */
public interface DataLoader<T> extends ContextMetaFactory<DataNode<T>>, Named {

    static DataLoader getFactory(Context context, String name) {
        return context.serviceStream(DataLoader.class)
                .filter(it -> it.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NameNotFoundException(name, "Data loader with given name not found"));
    }
}
