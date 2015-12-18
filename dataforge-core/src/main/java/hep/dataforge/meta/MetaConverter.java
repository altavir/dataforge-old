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
package hep.dataforge.meta;

import java.text.ParseException;

/**
 * An interface to convert meta to any other typed hierarchical value tree
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
@Deprecated
public interface MetaConverter<T> {

    /**
     * Get an object from meta
     *
     * @param an a {@link hep.dataforge.meta.Meta} object.
     * @return a T object.
     */
    T fromMeta(Meta an);

    /**
     * Create meta from given object with name override
     *
     * @param name a {@link java.lang.String} object.
     * @param source a T object.
     * @throws java.text.ParseException if any.
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    Meta toMeta(String name, T source) throws ParseException;

    /**
     * Создает аннотацию с именем по-умолчанию (если объект содержит какое-то
     * имя, то оно, иначе анонимная аннотация)
     *
     * @param source a T object.
     * @throws java.text.ParseException if any.
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    Meta toMeta(T source) throws ParseException;
}
