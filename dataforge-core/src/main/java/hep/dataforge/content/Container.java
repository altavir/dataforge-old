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
package hep.dataforge.content;

/**
 * Контейнер для данных, не являющихся контентом
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public class Container<T> extends AbstractContent{
    
    private final T data;

    /**
     * <p>Constructor for Container.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param data a T object.
     */
    public Container(String name, T data) {
        super(name);
        this.data = data;
    }
    
    /**
     * <p>Getter for the field <code>data</code>.</p>
     *
     * @return a T object.
     */
    public T getData(){
        return data;
    }
    
    /**
     * <p>getStoredType.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class getStoredType(){
        return data.getClass();
    }
}
