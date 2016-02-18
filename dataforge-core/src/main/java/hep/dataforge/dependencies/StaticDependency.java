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
package hep.dataforge.dependencies;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Names;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * The static dependency for several data pieces one of which should be default.
 *
 * @author Alexander Nozik
 */
public class StaticDependency implements Dependency {

    private final Map<String, Object> data;
//    private final Map<String, Class> types;
    private final String defaultDataKey;
    private final String name;

    public StaticDependency(String name, Map<String, Object> data, String defaultDataKey) {
        this.name = name;
        this.data = new HashMap<>(data);
//        this.types = new HashMap<>();

        if (!data.keySet().contains(defaultDataKey)) {
            throw new IllegalArgumentException("The default key should be present in the data");
        }

        this.defaultDataKey = defaultDataKey;
//        data.entrySet().stream().forEach((entry) -> {
//            this.types.put(entry.getKey(), entry.getValue().getClass());
//        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object get() {
        return data.get(defaultDataKey);
    }

    @Override
    public Object get(String key) {
        if (this.data.containsKey(key)) {
            return this.data.get(key);
        } else if (defaultDataKey.equals(key)) {
            return null;
        } else {
            throw new NameNotFoundException(key);
        }
    }

    @Override
    public Future getInFuture() {
        return new ConstantFuture<>(get());
    }

    @Override
    public Future getInFuture(String key) {
        return new ConstantFuture<>(get(key));
    }

    @Override
    public Class type(String key) {
        if (this.data.containsKey(key)) {
            return this.data.get(key).getClass();
        } else if (defaultDataKey.equals(key)) {
            return null;
        } else {
            throw new NameNotFoundException(key);
        }
    }

    @Override
    public Class type() {
        return type(defaultDataKey);
    }

    @Override
    public Names keys() {
        return Names.of(data.keySet());
    }

}
