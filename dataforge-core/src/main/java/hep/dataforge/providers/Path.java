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
package hep.dataforge.providers;

import hep.dataforge.names.Name;

/**
 * <p>
 * Path interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Path {

    String TARGET_EMPTY = "";
    String PATH_SEGMENT_SEPARATOR = "/";
    String TARGET_SEPARATOR = "::";

    static Path of(String path) {
        SegmentedPath p = new SegmentedPath(path);
        if (p.hasTail()) {
            return p;
        } else {
            return p.head();
        }
    }

    /**
     * Create a path with given target override (even if it is provided by the path itself)
     * @param target
     * @param path
     * @return
     */
    static Path of(String target, String path) {
        return of(path).withTarget(target);
    }

    /**
     * Является ли этот путь односегментным(конечным)
     *
     * @return a boolean.
     */
    boolean hasTail();

    /**
     * The Name of first segment
     *
     * @return a {@link hep.dataforge.names.Name} object.
     */
    Name name();

    default String nameString() {
        return name().toString();
    }

    /**
     * The path without first segment. Null if this is single segment path.
     *
     * @return
     */
    Path tail();

    /**
     * The target of first segment
     *
     * @return a {@link java.lang.String} object.
     */
    String target();

    /**
     * Return new path with different target
     *
     * @return
     */
    Path withTarget(String target);

}
