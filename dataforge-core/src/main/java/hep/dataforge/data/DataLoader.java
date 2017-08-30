package hep.dataforge.data;

import hep.dataforge.names.Named;
import hep.dataforge.utils.ContextMetaFactory;

/**
 * A common interface for data providers
 * Created by darksnake on 02-Feb-17.
 */
public interface DataLoader<T> extends ContextMetaFactory<DataNode<T>>, Named {
    DataLoader<?> SMART = new SmartDataLoader();

}