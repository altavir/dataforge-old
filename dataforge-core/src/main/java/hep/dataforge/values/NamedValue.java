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

import hep.dataforge.content.AbstractContent;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Content value
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class NamedValue extends AbstractContent implements Value {

    private final Value value;

    /**
     * <p>
     * Constructor for NamedValue.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link hep.dataforge.values.Value} object.
     */
    public NamedValue(String name, Value value) {
        super(name);
        this.value = value;
    }

    /**
     * <p>
     * getSourceValue.</p>
     *
     * @return a {@link hep.dataforge.values.Value} object.
     */
    public Value getSourceValue() {
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
    public int compareTo(Value o) {
        return value.compareTo(o);
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
    public ValueType valueType() {
        return value.valueType();
    }

//    /** {@inheritDoc}
//     * @return  */
//    @Override
//    public Content referenceValue() {
//        return this;
//    }
}
