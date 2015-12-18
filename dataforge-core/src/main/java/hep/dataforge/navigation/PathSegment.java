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

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.names.Name;
import static hep.dataforge.navigation.SegmentedPath.TARGET_EMPTY;
import static hep.dataforge.navigation.SegmentedPath.TARGET_SEPARATOR;

/**
 * Сегмент пути. Представляет собой пару цель::имя. Если цель не указана или
 * пустая, используется цель по-умолчанию для данного провайдера
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
class PathSegment implements Path {

    private Name name;
    private String target;

    /**
     * <p>
     * Constructor for PathSegment.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param target a {@link java.lang.String} object.
     */
    public PathSegment(String name, String target) {
        this.name = Name.of(name);
        this.target = target;
    }

    public PathSegment(Name name, String target) {
        this.name = name;
        this.target = target;
    }
    
    

    /**
     * <p>
     * Constructor for PathSegment.</p>
     *
     * @param path a {@link java.lang.String} object.
     */
    public PathSegment(String path) {
        if (path == null || path.isEmpty()) {
            throw new NamingException("Empty path");
        }
        if (path.contains(TARGET_SEPARATOR)) {
            String[] split = path.split(TARGET_SEPARATOR, 2);
            this.target = split[0];
            this.name = Name.of(split[1]);
        } else {
            this.target = TARGET_EMPTY;
            this.name = Name.of(path);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTail() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Name name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path tail() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String target() {
        if (target == null) {
            return TARGET_EMPTY;
        } else {
            return target;
        }
    }

    @Override
    public Path setTarget(String target) {
        return new PathSegment(name, target);
    }
    
    
}
