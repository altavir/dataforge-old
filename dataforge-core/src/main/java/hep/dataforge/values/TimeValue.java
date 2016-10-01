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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 *
 * @author Alexander Nozik
 */
class TimeValue extends AbstractValue {

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.value);
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
        final TimeValue other = (TimeValue) obj;
        return Objects.equals(this.value, other.value);
    }

    private final Instant value;

    /**
     * <p>
     * Constructor for TimeValue.</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public TimeValue(String value) {
        this.value = LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
    }

    public TimeValue(LocalDateTime value) {
        this.value = value.toInstant(ZoneOffset.UTC);
    }

    public TimeValue(Instant value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean booleanValue() {
        return value.isAfter(Instant.MIN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number numberValue() {
        return value.toEpochMilli();
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
     */
    @Override
    public Instant timeValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueType valueType() {
        return ValueType.TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return value.toString();
    }
}
