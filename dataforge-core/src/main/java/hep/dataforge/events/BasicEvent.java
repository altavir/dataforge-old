/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.events;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import java.time.Instant;

public class BasicEvent implements Event {

    private final Meta meta;

    public BasicEvent(Meta meta) {
        this.meta = meta;
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
            time = Instant.now();
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
    public Meta meta() {
        return this.meta;
    }

}
