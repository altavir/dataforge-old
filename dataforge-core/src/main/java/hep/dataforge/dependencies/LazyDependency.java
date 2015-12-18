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

import hep.dataforge.names.Names;
import java.util.function.Supplier;

/**
 * The lazy dependency that is declared and calculated later
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class LazyDependency<T> implements Dependency<T> {

    private final Supplier<T> calculator;
    private final String name;

    public LazyDependency(String name, Supplier<T> calculator) {
        this.calculator = calculator;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T get() {
        return calculator.get();
    }

    @Override
    public <R> R get(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class type(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Names keys() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
