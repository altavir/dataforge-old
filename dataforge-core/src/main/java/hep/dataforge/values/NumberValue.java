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
        hash = 59 * hash + Objects.hashCode(this.value);
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
            return ValueUtils.NUMBER_COMPARATOR.compare(this.numberValue(), other.numberValue()) == 0;
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
    public boolean booleanValue() {
        return value.doubleValue() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number numberValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     *
     * Время в СЕКУНДАХ
     */
    @Override
    public Instant timeValue() {
        return Instant.ofEpochMilli(value.longValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueType valueType() {
        return ValueType.NUMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return stringValue();
    }
}
