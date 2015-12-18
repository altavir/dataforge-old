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

/**
 * Interface for passive object repositories. In order to store parametric
 * builders T should be ParametricFactory
 *
 * @author Alexander Nozik
 * @param <T>
 */
public interface Library<T> {

    default T get(String tag) {
        return get(VersionTag.fromString(tag));
    }

    default boolean has(String tag) {
        return has(VersionTag.fromString(tag));
    }

    T get(VersionTag tag);

    boolean has(VersionTag tag);

    void put(VersionTag tag, T factory);
}
