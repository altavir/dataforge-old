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

import hep.dataforge.names.Name;

/**
 * <p>
 * Path interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface Path {

    public static final String TARGET_EMPTY = "";
    public static final String PATH_SEGMENT_SEPARATOR = "/";
    public static final String TARGET_SEPARATOR = "::";

    public static Path of(String path) {
        SegmentedPath p = new SegmentedPath(path);
        if (p.hasTail()) {
            return p;
        } else {
            return p.head();
        }
    }
    
    public static Path of(String path, String target){
                SegmentedPath p = new SegmentedPath(path);
        if (p.hasTail()) {
            return p;
        } else {
            return p.head();
        }
    }

    /**
     * Является ли этот путь односегментным(конечным)
     *
     * @return a boolean.
     */
    public boolean hasTail();

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
    public Path tail();

    /**
     * The target of first segment
     *
     * @return a {@link java.lang.String} object.
     */
    String target();
    
    /**
     * Return new path with different target
     * @return 
     */
    Path setTarget(String target);

}