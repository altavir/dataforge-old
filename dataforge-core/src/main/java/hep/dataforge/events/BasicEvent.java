/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.events;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.DateTimeUtils;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BasicEvent implements Event {

    private final Meta meta;
    private final Map<String, WeakReference> referenceMap = new HashMap<>();

    public BasicEvent(Meta meta) {
        this.meta = meta;
    }

    public BasicEvent(Meta meta, Map<String, Object> objects) {
        this.meta = meta;
        objects.forEach((key, value) -> {
            referenceMap.put(key, new WeakReference(value));
        });
    }

    /**
     * Create an event with given basic parameters and additional meta data. All
     * values except type could be null or empty
     *
     * @param type
     * @param source
     * @param priority
     * @param time
     * @param additionalMeta
     */
    public BasicEvent(String type, String source, int priority, Instant time, Meta additionalMeta) {
        MetaBuilder builder = new MetaBuilder("event");
        if (additionalMeta != null) {
            builder.update(additionalMeta.getBuilder());
        }

        builder.setValue(EVENT_TYPE_KEY, type);

        if (time == null) {
            time = DateTimeUtils.now();
        }
        builder.setValue(EVENT_TIME_KEY, time);
        if (source != null && !source.isEmpty()) {
            builder.setValue(EVENT_SOURCE_KEY, source);
        }
        if (priority != 0) {
            builder.setValue(EVENT_PRIORITY_KEY, priority);
        }
        this.meta = builder.build();
    }

    @Override
    public Optional getReference(String tag) {
        return Optional.ofNullable(referenceMap.get(tag));
    }

    @Override
    public Meta meta() {
        return this.meta;
    }

}
