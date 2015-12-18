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

import hep.dataforge.exceptions.ChainPathNotSupportedException;
import hep.dataforge.names.Name;

/**
 * The general interface for any object provider, which implements DataForge
 * path and naming convention
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class AbstractProvider implements Provider {

    /**
     * Return an object with given path (recurrent chain paths included)
     *
     * @param path a {@link java.lang.String} object in the format of Path
     * @return a T object.
     */
    @Override
    public Object provide(Path path) {
        Object res = provide(path.target(), path.name());
        if (path.hasTail()) {
            if (res instanceof Provider) {
                Path tail = path.tail();
                if (tail.target().isEmpty()) {
                    tail = tail.setTarget(defaultChainTarget());
                }
                return ((Provider) res).provide(tail);
            } else {
                throw new ChainPathNotSupportedException();
            }
        } else {
            return res;
        }
    }

    @Override
    public boolean provides(Path path) {
        if (provides(path.target(), path.name())) {
            if (path.hasTail()) {
                Object res = provide(path.target(), path.name());
                if (res instanceof Provider) {
                    Path tail = path.tail();
                    if (tail.target().isEmpty()) {
                        tail = tail.setTarget(defaultChainTarget());
                    }
                    return ((Provider) res).provides(tail);
                } else {
                    throw new ChainPathNotSupportedException();
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Indicates if direct member is provided (no chain paths)
     *
     * @param target
     * @param name
     * @return
     */
    protected abstract boolean provides(String target, Name name);

    public boolean provides(String target, String name) {
        if (target == null || target.isEmpty()) {
            target = defaultTagrget();
        }
        return provides(target, Name.of(name));
    }

    /**
     * Provide direct members only (no chain paths)
     *
     * @param target
     * @param name
     * @return
     */
    protected abstract Object provide(String target, Name name);

    public Object provide(String target, String name) {
        if (target == null || target.isEmpty()) {
            target = defaultTagrget();
        }
        return this.provide(target, Name.of(name));
    }

    /**
     * Default target for this provider
     *
     * @return
     */
    protected String defaultTagrget() {
        return "";
    }

    /**
     * Default target for next chain segment
     *
     * @return
     */
    protected String defaultChainTarget() {
        return "";
    }
}
