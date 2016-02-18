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

import hep.dataforge.names.Names;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

/**
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class GenericDependency<T> implements Dependency<T> {

    //FIXME store result after calculation
    private String name;

    private Supplier<T> data;
    private Class type;

    private Map<String, Supplier> extraList = new ConcurrentHashMap<>();
    private Map<String, Class> extraTypeList = new ConcurrentHashMap<>();

    public static <T> Builder<T> builder(Supplier<T> data, Class type) {
        return new Builder(data, type);
    }

    public static <T> Builder<T> builder(T data) {
        return new Builder(data);
    }

    private GenericDependency() {
    }

    public GenericDependency(String name, Supplier<T> data, Class type, Map<String, Supplier> extraList, Map<String, Class> extraTypeList) {
        this.name = name;
        this.data = data;
        this.type = type;
        this.extraList = extraList;
        this.extraTypeList = extraTypeList;
    }

    @Override
    public Future<T> getInFuture() {
        return new FutureTask<>(() -> data.get());
    }

    //PENDING replace Future task by central ExecutorService?
    @Override
    public <R> Future<R> getInFuture(String key) {
        return new FutureTask<>(() -> {
            if (extraList.containsKey(key)) {
                return (R)extraList.get(key).get();
            } else if (key.equals(Dependency.DEFAULT_KEY)) {
                return (R)data.get();
            } else {
                return null;
            }
        });
    }

    @Override
    public Class type(String key) {
        if (key.equals(Dependency.DEFAULT_KEY)) {
            return type;
        } else {
            return extraTypeList.get(key);
        }
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public Names keys() {
        return Names.of(extraList.keySet());
    }

    @Override
    public String getName() {
        return name;
    }

    public static class Builder<T> {

        GenericDependency<T> dependency = new GenericDependency<>();

        public Builder(Supplier<T> data, Class type) {
            dependency.data = data;
            dependency.type = type;
        }

        public Builder(T data) {
            dependency.data = () -> data;
            dependency.type = data.getClass();
        }

        public <S> Builder putExtra(String name, Class<S> type, Supplier<S> sup) {
            dependency.extraList.put(name, sup);
            dependency.extraTypeList.put(name, type);
            return this;
        }

        public <S> Builder putExtra(String name, S data) {
            dependency.extraList.put(name, () -> data);
            dependency.extraTypeList.put(name, data.getClass());
            return this;
        }

        public Dependency<T> build(String name) {
            dependency.name = name;
            return dependency;
        }
    }
}
