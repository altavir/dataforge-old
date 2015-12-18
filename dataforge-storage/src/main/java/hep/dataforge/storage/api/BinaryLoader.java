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
import java.util.Collection;

/**
 * The binary loader contains one o several binary fragments with common loader annotation.
 * @author darksnake
 * @param <T>
 */
public interface BinaryLoader<T> extends Loader {

    public static final String BINARY_LOADER_TYPE = "binary";
    public static final String DEFAULT_FRAGMENT_NAME = "";
    
    public Collection<String> fragmentNames();
    
    public T pull(String fragmentName) throws StorageException;
    
    public void push(String fragmentName, T data) throws StorageException;
}
