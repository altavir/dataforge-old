package hep.dataforge.data;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;


/**
 * A data loader that delegates loading to a specific loader
 */
public class SmartDataLoader implements DataLoader {
    public static final String FACTORY_TYPE_KEY = "loader";

    @SuppressWarnings("unchecked")
    public static DataLoader<?> getFactory(Context context, Meta meta) {
        if (meta.hasValue("dataLoaderClass")) {
            try {
                return (DataLoader<Object>) Class.forName(meta.getString("dataLoaderClass")).newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        } else {
            return meta.optString(FACTORY_TYPE_KEY).flatMap(loader ->
                    context.serviceStream(DataLoader.class)
                            .filter(it -> it.getName().equals(loader))
                            .findFirst()
            ).orElse(new DataFactory(Object.class));
        }
    }

    @Override
    public String getName() {
        return "smart";
    }

    @Override
    public DataNode build(Context context, Meta meta) {
        return getFactory(context, meta).build(context, meta);
    }
}
