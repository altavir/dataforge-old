package hep.dataforge.tables;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.utils.MetaHolder;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Simple hash map based adapter
 */
public class BasicAdapter extends MetaHolder implements ValuesAdapter {

    private final Map<String, String> mappings = new HashMap<>(6);

    public BasicAdapter(Meta meta) {
        super(meta);
        updateMapping();
    }

    private void updateMapping() {
        MetaUtils.valueStream(getMeta()).forEach(pair -> {
            mappings.put(pair.getKey(), pair.getValue().stringValue());
//
//            if(pair.getKey().endsWith(".value")){
//                mappings.put(pair.getKey().replace(".value",""),pair.getValue().stringValue());
//            } else {
//                mappings.put(pair.getKey(), pair.getValue().stringValue());
//            }
        });
    }

    @Override
    public Optional<Value> optComponent(Values values, String component) {
        return values.optValue(getComponentName(component));
    }

    @Override
    public String getComponentName(String component) {
        return mappings.getOrDefault(component, component);
    }

    @Override
    public Stream<String> listComponents() {
        return mappings.keySet().stream();
    }

    @Override
    public Meta toMeta() {
        if (getClass() == BasicAdapter.class) {
            return getMeta();
        } else {
            //for custom adapters
            return getMeta().getBuilder().putValue("@class", getClass().getName());
        }
    }

    @Override
    public void fromMeta(Meta meta) {
        setMeta(meta);
        updateMapping();
    }
}
