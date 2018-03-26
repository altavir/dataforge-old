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

import hep.dataforge.Named;

import java.time.Instant;

/**
 * Content value
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class NamedValue implements Named, Value {

    public static NamedValue of(String name, Object value) {
        return new NamedValue(name, Value.of(value));
    }

    private final Value value;
    private final String name;

    public NamedValue(String name, Value value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Return underlying value without a name
     *
     * @return
     */
    public Value getAnonymousValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean booleanValue() {
        return value.booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number numberValue() {
        return value.numberValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        return value.stringValue();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Instant timeValue() {
        return value.timeValue();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public ValueType getType() {
        return value.getType();
    }

    @Override
    public Object value() {
        return value.value();
    }

}
