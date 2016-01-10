/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.events;

import hep.dataforge.meta.MetaBuilder;

/**
 * Th builder class for events
 * @author Alexander Nozik
 */
public class EventBuilder {
    private final MetaBuilder builder = new MetaBuilder("event");

    public EventBuilder(String type) {
        this.builder.setValue(Event.EVENT_TYPE_KEY, type);
    }
    
    
}
