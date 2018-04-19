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

import hep.dataforge.exceptions.ValueConversionException;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.Objects;

/**
 *
 * @author Alexander Nozik
 */
class NumberValue extends AbstractValue {

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 7;
        //TODO evaluate infinities
        hash = 59 * hash + Objects.hashCode(new BigDecimal(this.value.doubleValue(), MathContext.DECIMAL32));
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Value) {
            final Value other = (Value) obj;
            try {
                return ValueUtils.NUMBER_COMPARATOR.compare(this.getNumber(), other.getNumber()) == 0;
            }catch (ValueConversionException ex){
                return false;
            }
        } else {
            return super.equals(obj);
        }
    }

    private final Number value;

    public NumberValue(double d) {
        this.value = d;
    }

    public NumberValue(int i) {
        this.value = i;
    }

    public NumberValue(long l) {
        this.value = l;
    }

    public NumberValue(Number l) {
        this.value = l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBoolean() {
        return value.doubleValue() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number getNumber() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString() {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     *
     * Время в СЕКУНДАХ
     */
    @Override
    public Instant getTime() {
        return Instant.ofEpochMilli(value.longValue());
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ValueType getType() {
        return ValueType.NUMBER;
    }

    @Override
    public Object value() {
        return this.value;
    }
}
