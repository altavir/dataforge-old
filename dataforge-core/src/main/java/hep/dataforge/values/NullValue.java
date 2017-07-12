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

/**
 *
 * @author Alexander Nozik
 */
class NullValue implements Value {

    NullValue() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean booleanValue() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public double doubleValue() {
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number numberValue() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Instant timeValue() {
        return Instant.MIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        return "";
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public ValueType getType() {
        return ValueType.NULL;
    }

    @Override
    public Object value() {
        return null;
    }
}
