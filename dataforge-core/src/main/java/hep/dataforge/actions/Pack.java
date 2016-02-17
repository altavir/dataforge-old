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
package hep.dataforge.actions;

import hep.dataforge.dependencies.Dependency;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A default implementation of ActionResult interface. It contains data
 * dependencies, result annotation and global action log.
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class Pack<T> implements ActionResult<T> {

    private final String name;
    private final Meta annotation;
    private final Map<String, Dependency<T>> data;
    private final Logable log;
    private final Class<T> type;



    public Pack(String name,Logable log, Meta annotation, Class<T> type, Dependency<T>... data) {
        this(name, annotation, log, type, Arrays.asList(data));
    }

    public Pack(String name, Meta annotation, Logable log, Class<T> type, Iterable<Dependency<T>> data) {
        this.name = name;
        this.annotation = annotation;
        this.data = new HashMap<>();
        for (Dependency<T> dep : data) {
            this.data.put(dep.getName(), dep);
        }
        this.log = log;
        this.type = type;
    }

    public Pack(String name, Meta annotation, Logable log, Class<T> type, Map<String, Dependency<T>> data) {
        this.name = name;
        this.annotation = annotation;
        this.data = data;
        this.log = log;
        this.type = type;
    }
    
    

    @Override
    public Dependency<T> get(String path) {
        if(data.containsKey(path)){
            return data.get(path);
        } else {
            throw new NameNotFoundException(name);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public Iterator<Dependency<T>> iterator() {
        return data.values().iterator();
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public Logable log() {
        return log;
    }

    @Override
    public Meta meta() {
        return annotation;
    }

    
    
//    @Override
//    public Item<Dependency<T>> data() {
//        return data;
//    }

    @Override
    public int size() {
        return data.size();
    }

}
