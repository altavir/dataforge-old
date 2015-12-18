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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.WrongTargetException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.StateChangedEvent;
import hep.dataforge.storage.api.StateLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.MessageFactory;
import hep.dataforge.storage.commons.StorageMessageUtils;
import static hep.dataforge.storage.commons.StorageMessageUtils.ACTION_KEY;
import static hep.dataforge.storage.commons.StorageMessageUtils.PULL_OPERATION;
import static hep.dataforge.storage.commons.StorageMessageUtils.PUSH_OPERATION;
import hep.dataforge.values.Value;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

/**
 *
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
    public Value getValue(String name) {
        check();
        if (states.containsKey(name)) {
            return states.get(name);
        }
        if (defaults.containsKey(name)) {
            return defaults.get(name);
        } else {
            throw new NameNotFoundException(name);
        }
    }

    protected boolean isUpToDate() {
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
            if (!acceptEnvelope(message)) {
                return StorageMessageUtils.exceptionResponse(message, new WrongTargetException());
            }
            Meta meta = message.meta();
            String operation = meta.getString(ACTION_KEY);
            EnvelopeBuilder res = new MessageFactory().responseBase(message);
            switch (operation) {
                case PUSH_OPERATION:
                case "set":
                    if (meta.hasNode("state")) {
                        for (Meta state : meta.getNodes("state")) {
                            String stateName = state.getString("name");
                            String stateValue = state.getString("value");
                            setValue(stateName, stateValue);
                            res.putMetaNode(new MetaBuilder("state")
                                    .putValue("name", stateName)
                                    .putValue("value", stateValue));
                        }
                    } else if (meta.hasValue("state")) {
                        String stateName = meta.getString("name");
                        String stateValue = meta.getString("value");
                        setValue(stateName, stateValue);
                        res.putMetaNode(new MetaBuilder("state")
                                .putValue("name", stateName)
                                .putValue("value", stateValue));
                    }

                    return res.build();
                case PULL_OPERATION:
                case "get":
                    String[] names;
                    if (meta.hasValue("name")) {
                        names = meta.getStringArray("name");
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
    public void setValue(String name, Value value) throws StorageException {
        check();        
        Value oldValue = states.get(name);
        if (oldValue == null) {
            oldValue = Value.getNull();
        }
        states.put(name, value);
        EventLoader el = getEventLoader();
        if (el != null) {
            el.push(new BasicStateChangedEvent(name, oldValue, value));
        }
        commit();
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

    private class BasicStateChangedEvent implements StateChangedEvent {

        private final String name;
        private final Value oldState;
        private final Value newState;
        private final Instant time;

        public BasicStateChangedEvent(String name, Value oldState, Value newState) {
            this.name = name;
            this.oldState = oldState;
            this.newState = newState;
            time = Instant.now();
        }

        @Override
        public StateLoader loader() {
            return AbstractStateLoader.this;
        }

        @Override
        public Value oldState() {
            return oldState;
        }

        @Override
        public Value newState() {
            return newState;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public String type() {
            return "storage.stateChanged";
        }

        @Override
        public String source() {
            if (getStorage() != null) {
                return getStorage().getName() + "." + getName();
            } else {
                return getName();
            }
        }

        @Override
        public String stateName() {
            return name;
        }

        @Override
        public Instant time() {
            return time;
        }

        @Override
        public String toString() {
            return String.format("(%s) [%s] : changed state '%s' from %s to %s", time().toString(), source(), stateName(), oldState().stringValue(), newState().stringValue());
        }

    }
}
