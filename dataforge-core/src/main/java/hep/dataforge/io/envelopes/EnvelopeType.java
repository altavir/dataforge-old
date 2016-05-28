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
package hep.dataforge.io.envelopes;

import hep.dataforge.values.CompositePropertyValue;
import hep.dataforge.values.Value;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Alexander Nozik
 * @param <T>
 */
public interface EnvelopeType<T extends Envelope> {

    short getCode();

    String getName();

    default Value getValue() {
        return new CompositePropertyValue(getCode(), getName());
    }

    String description();
    
    default Map<String, Value> defaultProperties(){
        return Collections.emptyMap();
    }

    EnvelopeReader<T> getReader();

    EnvelopeWriter<T> getWriter();

    /**
     * True if metadata lenth autodetection is allowed
     *
     * @return
     */
    boolean infiniteMetaAllowed();

    /**
     * True if data lenth autodetection is allowed
     *
     * @return
     */
    boolean infiniteDataAllowed();

}
