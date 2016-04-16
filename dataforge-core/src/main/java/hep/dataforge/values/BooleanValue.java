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
import java.time.Instant;

/**
 *
 * @author Alexander Nozik
 */
class BooleanValue implements Value {

    public static Value TRUE = new BooleanValue(true);
    public static Value FALSE = new BooleanValue(false);

    public static Value ofBoolean(boolean b) {
        if (b) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    public static Value ofBoolean(String b) {
        return ofBoolean(Boolean.parseBoolean(b));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.value ? 1 : 0);
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BooleanValue other = (BooleanValue) obj;
        return this.value == other.value;
    }

    private final boolean value;

    private BooleanValue(boolean value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean booleanValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Value o) {
        return Boolean.compare(value, o.booleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number numberValue() {
        if (value) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        return Boolean.toString(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant timeValue() {
        throw new ValueConversionException(this, ValueType.BOOLEAN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueType valueType() {
        return ValueType.BOOLEAN;
    }

    @Override
    public String toString() {
        return stringValue();
    }
}
