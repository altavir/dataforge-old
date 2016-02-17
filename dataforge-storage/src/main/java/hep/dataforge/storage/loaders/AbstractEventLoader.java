/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.storage.loaders;

import hep.dataforge.events.Event;
import hep.dataforge.events.EventHandler;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Storage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javafx.util.Pair;

/**
 * File implementation of EventLoader
 * @author Alexander Nozik
 * @param <T>
 */
public abstract class AbstractEventLoader<T extends Event> extends AbstractLoader implements EventLoader<T> {

    private final Map<String, Pair<Predicate<T>, EventHandler<T>>> listeners = new HashMap<>();

    public AbstractEventLoader(Storage storage, String name, Meta annotation) {
        super(storage, name, annotation);
    }

    @Override
    public Envelope respond(Envelope message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Listeners are evaluated before physical push
     * @param event
     * @throws StorageException 
     */
    @Override
    public void push(T event) throws StorageException {
        for(Pair<Predicate<T>, EventHandler<T>> pair : listeners.values()){
            if(pair.getKey()!= null && pair.getKey().test(event)){
                pair.getValue().handle(event);
            }
        }
        pushDirect(event);
    }

    protected abstract void pushDirect(T event) throws StorageException;

    @Override
    public void addEventListener(String name, Predicate<T> condition, EventHandler<T> handler) {
        this.listeners.put(name, new Pair<>(condition, handler));
    }

    @Override
    public void removeEventListener(String name) {
        this.listeners.remove(name);
    }

    @Override
    public String getType() {
        return EVENT_LOADER_TYPE;
    }
    
    

}
