package hep.dataforge.context;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by darksnake on 10-Apr-17.
 */
public abstract class AbstractPluginRepository implements PluginRepository {
    @Override
    public Optional<Plugin> opt(PluginTag tag) {
        return stream().filter(plugin -> tag.matches(plugin.getTag()))
                .sorted(this::compare)
                .findFirst();
    }

    protected int compare(Plugin p1, Plugin p2) {
        return Integer.compare(p1.getTag().getInt("priority", 0), p2.getTag().getInt("priority", 0));
    }

    @Override
    public List<PluginTag> listTags() {
        return stream().map(Plugin::getTag).collect(Collectors.toList());
    }

    protected abstract Stream<Plugin> stream();
}
