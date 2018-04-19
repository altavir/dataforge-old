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
package hep.dataforge.values;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * A lazy calculated value defined by some supplier. The value is calculated
 * only once on first call, after that it is stored and not recalculated.
 * <p>
 * <strong>WARNING</strong> Since the value is calculated on demand it is not
 * strictly immutable. Use it only then it is impossible to avoid or ensure that
 * supplier does not depend on external state.
 * </p>
 *
 * @author Darksnake
 */
public class CalculatedValue implements Value {

    private final ValueType type;
    private final Supplier<Value> supplier;
    private Value value;

    public CalculatedValue(ValueType type, Supplier<Value> supplier) {
        this.type = type;
        this.supplier = supplier;
    }

    private void calculate() {
        this.value = supplier.get();
    }

    private Value getValue() {
        if (this.value == null) {
            calculate();
        }
        return value;
    }

    @Override
    public Number getNumber() {
        return getValue().getNumber();
    }

    @Override
    public boolean getBoolean() {
        return getValue().getBoolean();
    }

    @Override
    public Instant getTime() {
        return getValue().getTime();
    }

    @Override
    public String getString() {
        return getValue().getString();
    }

    @NotNull
    @Override
    public ValueType getType() {
        return type;
    }

    @Override
    public Object value() {
        return getValue().value();
    }
}
