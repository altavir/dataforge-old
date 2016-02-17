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
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Names;

/**
 * A generalized dependency could be static data or task to be performed on
 * demand.
 * <p>
 * The dependency could have several pieces of data under different keys. It
 * also has a default key which can contain separate data or link to one of the
 * keys.
 * </p>
 * <p>
 * The type of dependency corresponds to the type returned for default key
 * </p>
 * <p>
 * Get methods should always provide something (even null) for default key, even
 * if it is not present in {@code keys()}, but could throw
 * {@code NameNotFoundException} for other keys.
 * </p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 * @param <T>
 */
public interface Dependency<T> extends Named, Annotated {

    public static final String DEFAULT_KEY = "";
    public static final String META_KEY = "meta";

    /**
     * The data provided by this dependency for default key. Could be stored
     * statically or obtained on-demand. Equivalent of get("").
     *
     * @return a {@link java.lang.Object} object.
     */
    T get();

    /**
     * Get data for given key. Return null if given key is not present.
     *
     * @param <R>
     * @param key
     * @return
     */
    <R> R get(String key);

    /**
     * Get the declared type for given key
     *
     * @param key
     * @return
     */
    Class type(String key);

    @SuppressWarnings("unchecked")
    default Class<T> type() {
        return type(DEFAULT_KEY);
    }

    /**
     * The set of present keys
     *
     * @return
     */
    Names keys();

    default boolean isValid() {
        return true;
    }
    
    @Override
    default Meta meta(){
        Meta res = this.<Meta>get(META_KEY);
        if(res != null){
            return res;
        } else {
            return Meta.buildEmpty(META_KEY);
        }
    }
}
