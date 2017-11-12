package hep.dataforge.control;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;

/**
 * A factory SPI class for connections
 */
public interface ConnectionFactory {
    String getType();

    /**
     *
     * @param obj an object for which this connections is intended
     * @param context context of the connection (could be different from connectible context)
     * @param meta configuration for connection
     * @param <T> type of the connectible
     * @return
     */
    <T extends Connectible> Connection build(T obj, Context context, Meta meta);
}
