package hep.dataforge.context;

import hep.dataforge.meta.Meta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by darksnake on 10-Apr-17.
 */
public abstract class AbstractPluginLoader implements PluginLoader {
    @Override
    public Optional<Plugin> opt(PluginTag tag, Meta meta) {
        return factories().filter(factory -> tag.matches(factory.getTag()))
                .sorted(this::compare)
                .findFirst().map(it -> it.build(meta));
    }

    protected int compare(PluginFactory p1, PluginFactory p2) {
        return Integer.compare(p1.getTag().getInt("priority", 0), p2.getTag().getInt("priority", 0));
    }

    @Override
    public List<PluginTag> listTags() {
        return factories().map(PluginFactory::getTag).collect(Collectors.toList());
    }

    protected abstract Stream<PluginFactory> factories();
}
