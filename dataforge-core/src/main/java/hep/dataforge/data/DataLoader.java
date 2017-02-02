package hep.dataforge.data;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;

/**
 * Created by darksnake on 02-Feb-17.
 */
public interface DataLoader<T> {
    DataNode<T> load(Context context, Meta dataConfig);
}
