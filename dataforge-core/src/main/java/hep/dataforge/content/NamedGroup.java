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

import hep.dataforge.exceptions.ContentException;
import hep.dataforge.meta.Annotated;
import java.util.List;

/**
 * A named group of objects of the same type. Can store additional group data in
 * the annotation.
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public interface NamedGroup<T> extends Named, Annotated, Iterable<T> {

    T get(String name) throws ContentException;

    boolean has(String name);

    Class<T> type();

    boolean isEmpty();

    List<T> asList();
}
