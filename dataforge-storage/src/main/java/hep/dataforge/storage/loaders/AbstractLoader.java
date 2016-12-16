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
import hep.dataforge.io.messages.MessageValidator;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.EventLoader;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.StorageUtils;

import static hep.dataforge.storage.commons.AbstractStorage.LOADER_TARGET_TYPE;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractLoader implements Loader {

    private final String name;
    protected Meta meta;
    protected boolean readOnly = false;
    private final Storage storage;

    public AbstractLoader(Storage storage, String name, Meta annotation) {
        this.name = name;
        this.meta = annotation;
        this.storage = storage;
    }

    public AbstractLoader(Storage storage, String name) {
        this.name = name;
        this.storage = storage;
    }

    @Override
    public void close() throws Exception {
        meta = null;
    }

    @Override
    public Meta meta() {
        return meta;
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

    /**
     * Loader meta must be set here if it is not set by constructor
     *
     * @throws Exception
     */
    @Override
    public abstract void open() throws Exception;

    @Override
    public boolean isOpen() {
        return meta != null;
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
    protected EventLoader<?> getEventLoader() throws StorageException {
        if (getStorage() != null) {
            return getStorage().getDefaultEventLoader();
        } else {
            return null;
        }
    }

    public MessageValidator getValidator() {
        return StorageUtils.defaultMessageValidator(LOADER_TARGET_TYPE, getName());
    }

    protected void checkOpen() {
        if (!isOpen()) {
            try {
                open();
            } catch (Exception ex) {
                throw new RuntimeException("Can't open loader", ex);
            }
        }
    }
}
