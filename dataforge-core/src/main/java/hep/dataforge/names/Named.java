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
package hep.dataforge.names;

/**
 * Any object that have name
 *
 * @author Alexander Nozik
 */
public interface Named {

    /**
     * Get the name of given object. If object is Named its name is used,
     * otherwise, use Object.toString
     *
     * @param obj
     * @return
     */
    public static String nameOf(Object obj) {
        if (obj instanceof Named) {
            return ((Named) obj).getName();
        } else {
            return obj.toString();
        }
    }

    /**
     * The name of this object instance
     *
     * @return
     */
    String getName();

    /**
     * Проеверка аноанимности (безымянности) контента. Контент считается
     * анонимным если его name равен null или пустой строке.
     *
     * Анонимный контент в качестве аргумента запрещен в некоторых методах
     *
     * @return
     */
    default boolean isAnonimous() {
        return (this.getName() == null || this.getName().equals(""));
    }
}
