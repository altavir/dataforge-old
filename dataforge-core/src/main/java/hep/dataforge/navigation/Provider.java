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
package hep.dataforge.navigation;

/**
 *
 * @author Alexander Nozik
 */
public interface Provider {

    /**
     * Provides by path generated from string
     *
     * @param path
     * @return
     */
    default Object provide(String path) {
        return this.provide(Path.of(path));
    }

    /**
     * Type checked provide
     *
     * @param path
     * @param type
     * @param <T>
     * @return
     */
    default <T> T provide(String path, Class<T> type) {
        return provide(Path.of(path), type);
    }

    /**
     * Type checked provide
     *
     * @param <T>
     * @param path
     * @param type
     * @return
     */
    default <T> T provide(Path path, Class<T> type) {
        Object obj = provide(path);
        if (type.isInstance(obj)) {
            return (T) obj;
        } else {
            throw new IllegalStateException(
                    String.format("Error in type checked provide method. %s object expected, but %s provided", 
                            type.getName(), obj.getClass().getName())
            );
        }
    }

    default boolean provides(String path) {
        return this.provides(Path.of(path));
    }

    /**
     * Return an object with given path (recurrent chain paths included)
     *
     * @param path a {@link java.lang.String} object in the format of Path
     * @return a T object.
     */
    Object provide(Path path);

    /**
     * <p>
     * provides.</p>
     *
     * @param path
     * @return a boolean.
     */
    boolean provides(Path path);

}
