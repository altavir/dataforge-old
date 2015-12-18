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
package hep.dataforge.storage.api;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.util.Set;

/**
 * State loader is
 *
 * @author darksnake
 */
public interface StateLoader extends Loader, ValueProvider {

    public static final String STATE_LOADER_TYPE = "state";

    /**
     * Change the state and generate corresponding StateChangedEvent
     *
     * @param path
     * @param value
     * @throws hep.dataforge.exceptions.StorageException
     */
    void setValue(String path, Value value) throws StorageException;

    default void setValue(String path, Object value) throws StorageException {
        setValue(path, Value.of(value));
    }

    @Override
    public Value getValue(String path);

    /**
     * List of all available state names (including default values if they are
     * available)
     *
     * @return
     */
    public Set<String> getStateNames();
    
}
