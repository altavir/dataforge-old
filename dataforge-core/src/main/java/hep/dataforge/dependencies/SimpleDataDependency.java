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

import hep.dataforge.content.Named;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Names;

/**
 * A simple static dependency wrapper for single named data piece. Data is
 * passed in constructor. Is not workspace dependent.
 * <p>
 * Null check is not made in constructor. Instead {@code isValid()} method
 * returns false is data is null. 
 * </p>
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public class SimpleDataDependency<T extends Named> implements Dependency<T> {

    private final T data;

    /**
     * <p>
     * Constructor for StaticDependency.</p>
     *
     * @param data a T object.
     */
    public SimpleDataDependency(T data) {
        this.data = data;
    }

    @Override
    public String getName() {
        return data.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() {
        return data;
    }

    @Override
    public Meta meta() {
        if(data instanceof Annotated){
            return ((Annotated)data).meta();
        } else {
            return Meta.buildEmpty(getName());
        }
    }
    
    

    @Override
    public <R> R get(String key) {
        if (key.equals(DEFAULT_KEY)) {
            return (R) get();
        } else {
            throw new NameNotFoundException(key);
        }
    }

    @Override
    public Class type(String key) {
        if (key.equals(DEFAULT_KEY)) {
            return data.getClass();
        } else {
            throw new NameNotFoundException(key);
        }
    }

    @Override
    public Names keys() {
        return Names.of("");
    }

    @Override
    public boolean isValid() {
        return data != null;
    }

}
