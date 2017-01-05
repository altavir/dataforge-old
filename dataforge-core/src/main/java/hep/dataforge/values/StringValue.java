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

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * TODO заменить интерфейсы на используемые в javax.jcr
 *
 * @author Alexander Nozik
 */
class StringValue extends AbstractValue {

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.value);
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
            return Objects.equals(this.stringValue(), other.stringValue());
        } else {
            return super.equals(obj);
        }
    }

    private final String value;

    /**
     * Если передается строка в кавычках, то кавычки откусываются
     *
     * @param value a {@link java.lang.String} object.
     */
    public StringValue(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            this.value = value.substring(1, value.length() - 1);
        } else {
            this.value = value;
        }
    }

//    public StringValue(Boolean value) {
//        this.value = value.toString();
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean booleanValue() {
        try {
            return Boolean.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new ValueConversionException(this, ValueType.BOOLEAN);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number numberValue() {
        try {
            return NumberFormat.getInstance().parse(value);
        } catch (ParseException | NumberFormatException ex) {
            throw new ValueConversionException(this, ValueType.NUMBER);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Всегда возвращаем строковое значение строки в ковычках чтобы избежать
     * проблем с пробелами
     */
    @Override
    public String stringValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant timeValue() {
        try {
            if (value.endsWith("Z")) {
                return Instant.parse(value);
            } else {
                return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
            }
        } catch (DateTimeParseException ex) {
            throw new ValueConversionException(this, ValueType.TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueType valueType() {
        return ValueType.STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

}
