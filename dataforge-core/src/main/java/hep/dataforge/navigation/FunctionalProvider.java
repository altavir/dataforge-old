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

import hep.dataforge.exceptions.TargetNotProvidedException;
import hep.dataforge.names.Name;
import java.util.function.Function;

/**
 * <p>FunctionalProvider class.</p>
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public class FunctionalProvider<T> extends AbstractProvider {

    private final Function<String, T> getter;
    private final Function<String, Boolean> checker;
    private final String target;

    /**
     * <p>Constructor for FunctionalProvider.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @param getter a {@link java.util.function.Function} object.
     * @param checker a {@link java.util.function.Function} object.
     */
    public FunctionalProvider(String target, Function<String, T> getter, Function<String, Boolean> checker) {
        this.getter = getter;
        this.checker = checker;
        this.target = target;
    }

    /** {@inheritDoc} */
    @Override
    public T provide(String target, Name name) {
        if (!this.target.equals(target)) {
            throw new TargetNotProvidedException();
        } else {
            return getter.apply(name.toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean provides(String target, Name name) {
        if (!this.target.equals(target)) {
            return false;
        } else {
            return checker.apply(name.toString());
        }
    }

}
