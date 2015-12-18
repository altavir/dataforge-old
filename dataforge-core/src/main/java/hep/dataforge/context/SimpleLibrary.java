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
package hep.dataforge.context;

import java.util.HashMap;
import java.util.Map;


public class SimpleLibrary<T> implements Library<T> {
    
    private final Map<VersionTag, T> lib = new HashMap<>();

    public SimpleLibrary() {
    }

    public SimpleLibrary(SimpleLibrary<T> source) {
        this.lib.putAll(source.lib);
    }
    
    @Override
    public T get(VersionTag tag) {
        return lib.get(tag);
    }

    @Override
    public boolean has(VersionTag tag) {
        return lib.containsKey(tag);
    }

    @Override
    public void put(VersionTag tag, T factory) {
        lib.put(tag, factory);
    }
    
    public void put(String tag, T factory) {
        lib.put(VersionTag.fromString(tag), factory);
    }    
    
}
