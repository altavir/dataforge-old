package hep.dataforge.data;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Named;
import hep.dataforge.utils.ContextMetaFactory;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * A common interface for data providers
 * Created by darksnake on 02-Feb-17.
 */
public interface DataLoader<T> extends ContextMetaFactory<DataNode<T>>, Named {
    static <T> DataLoader<T> getFactory(String name) {
        return StreamSupport
                .stream(ServiceLoader.load(DataLoader.class).spliterator(), false)
                .filter(it -> it.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NameNotFoundException(name, "Data loader with given name not found"));
    }
}
