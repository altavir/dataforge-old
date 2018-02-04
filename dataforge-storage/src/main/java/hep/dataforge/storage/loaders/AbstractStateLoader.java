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
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.WrongTargetException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.StateChangedEvent;
import hep.dataforge.storage.api.StateLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.MessageFactory;
import hep.dataforge.storage.commons.StorageMessageUtils;
import hep.dataforge.utils.Optionals;
import hep.dataforge.values.Value;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static hep.dataforge.storage.commons.StorageMessageUtils.*;

/**
 * @author darksnake
 */
public abstract class AbstractStateLoader extends AbstractLoader implements StateLoader {

    protected final Map<String, Value> states = new ConcurrentHashMap<>();
    protected Map<String, Value> defaults = new HashMap<>();
    protected boolean upToDate = false;

    public AbstractStateLoader(Storage storage, String name, Meta annotation) {
        super(storage, name, annotation);
    }

    /**
     * the default values are not stored in the database and should be defined
     * in program
     *
     * @param name
     * @param value
     */
    protected void setDefaultValue(String name, Value value) {
        this.defaults.put(name, value);
    }

    protected void setDefaults(Map<String, Value> defaults) {
        this.defaults = defaults;
    }

    @Override
    public Optional<Value> optValue(@NotNull String name) {
        check();
        return Optionals.either(Optional.ofNullable(states.get(name)))
                .or(() -> Optional.ofNullable(defaults.get(name)))
                .opt();
    }

    protected final boolean isUpToDate() {
        return upToDate;
    }

    @Override
    public boolean hasValue(String name) {
        check();
        return states.containsKey(name) || defaults.containsKey(name);
    }

    @Override
    public boolean isEmpty() {
        check();
        return states.isEmpty();
    }

    @Override
    public Envelope respond(Envelope message) {
        check();
        try {
            if (!getValidator().isValid(message)) {
                return StorageMessageUtils.exceptionResponse(message, new WrongTargetException());
            }
            Meta envelopeMeta = message.getMeta();
            String operation = envelopeMeta.getString(ACTION_KEY);
            EnvelopeBuilder res = new MessageFactory().responseBase(message);
            switch (operation) {
                case PUSH_OPERATION:
                case "set":
                    if (envelopeMeta.hasMeta("state")) {
                        for (Meta state : envelopeMeta.getMetaList("state")) {
                            String stateName = state.getString("name");
                            String stateValue = state.getString("value");
                            pushState(stateName, stateValue);
                            res.putMetaNode(new MetaBuilder("state")
                                    .putValue("name", stateName)
                                    .putValue("value", stateValue));
                        }
                    } else if (envelopeMeta.hasValue("state")) {
                        String stateName = envelopeMeta.getString("name");
                        String stateValue = envelopeMeta.getString("value");
                        pushState(stateName, stateValue);
                        res.putMetaNode(new MetaBuilder("state")
                                .putValue("name", stateName)
                                .putValue("value", stateValue));
                    }

                    return res.build();
                case PULL_OPERATION:
                case "get":
                    String[] names;
                    if (envelopeMeta.hasValue("name")) {
                        names = envelopeMeta.getStringArray("name");
                    } else {
                        names = getStateNames().toArray(new String[0]);
                    }
                    for (String stateName : names) {
                        if (hasValue(stateName)) {
                            String stateValue = getString(stateName);
                            res.putMetaNode(new MetaBuilder("state")
                                    .putValue("name", stateName)
                                    .putValue("value", stateValue));
                        }
                    }

                    return res.build();

                default:
                    throw new NotDefinedException("Unknown operation");
            }

        } catch (StorageException | UnsupportedOperationException | NotDefinedException ex) {
            return StorageMessageUtils.exceptionResponse(message, ex);
        }
    }

    @Override
    public void pushState(String name, Value value) throws StorageException {
        check();
        Value oldValue = states.get(name);
        if (oldValue == null) {
            oldValue = Value.getNull();
        }
        states.put(name, value);
        commit();
        Event event = StateChangedEvent.build(name, oldValue, value);
        forEachConnection("eventListener",EventHandler.class, handler -> {
            handler.pushEvent(event);
        });
    }

    @Override
    public String getType() {
        return STATE_LOADER_TYPE;
    }

    protected abstract void commit() throws StorageException;

    protected abstract void update() throws StorageException;

    /**
     * Check if loader is upToDate and update it if necessarily
     */
    private void check() {
        if (!isUpToDate()) {
            try {
                LoggerFactory.getLogger(getClass()).debug("Bringing state loader up to date");
                update();
            } catch (StorageException ex) {
                throw new RuntimeException("Can't update state loader");
            }
        }
    }

    @Override
    public Set<String> getStateNames() {
        check();
        return states.keySet();
    }

}
