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

import hep.dataforge.exceptions.PushFailedException;
import hep.dataforge.exceptions.StorageException;
import static hep.dataforge.io.envelopes.Dispatcher.TARGET_NAME_KEY;
import static hep.dataforge.io.envelopes.Dispatcher.TARGET_TYPE_KEY;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import static hep.dataforge.storage.commons.AbstractStorage.LOADER_TARGET_TYPE;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractLoader implements Loader {

    private final String name;
    private final Meta annotation;
    protected boolean readOnly = false;
    private final Storage storage;

    public AbstractLoader(Storage storage, String name, Meta annotation) {
        this.name = name;
        this.annotation = annotation;
        this.storage = storage;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public Meta meta() {
        return annotation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return meta().getString("description", "");
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly || meta().getBoolean("readonly", false);
    }

    @Override
    public void open() {

    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String getType() {
        return meta().getString(LOADER_TYPE_KEY, PointLoader.POINT_LOADER_TYPE);
    }

    protected void tryPush() throws PushFailedException {
        if (isReadOnly()) {
            throw new PushFailedException(this, "Trying to push to read only loader.");
        }
    }

    /**
     * An event loader associated with this loader.
     *
     * @return
     * @throws hep.dataforge.exceptions.StorageException
     */
    protected EventLoader getEventLoader() throws StorageException {
        if (getStorage() != null) {
            return getStorage().getDefaultEventLoader();
        } else {
            return null;
        }
    }

    /**
     * Check if this loader is the target for given envelope
     *
     * @param envelope
     * @return
     */
    @Override
    public boolean acceptEnvelope(Envelope envelope) {
        if (envelope.meta().hasNode(ENVELOPE_TARGET_NODE)) {
            Meta target = envelope.meta().getNode(ENVELOPE_TARGET_NODE);
            String targetType = target.getString(TARGET_TYPE_KEY, LOADER_TARGET_TYPE);
            if(targetType.equals(LOADER_TARGET_TYPE)){
                String targetName = target.getString(TARGET_NAME_KEY);
                return targetName.endsWith(getName());
            } else {
                return false;
            }
        } else {
            LoggerFactory.getLogger(getClass()).debug("Envelope does not have target. Acepting by default.");
            return true;
        }
    }
    
    

//    @Override
//    public Envelope respond(Envelope message) {
//        try {
//            return StorageMessageUtils.evaluateRequest(this, message);
//        } catch (StorageException ex) {
//            return exceptionResponse(message, ex);
//        }
//    }

    @Override
    public Meta targetDescription() {
        return new MetaBuilder(ENVELOPE_TARGET_NODE)
                .putValue(TARGET_TYPE_KEY, LOADER_TARGET_TYPE)
                .putValue(TARGET_NAME_KEY, getName())
                .build();
    }
}
