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

import hep.dataforge.values.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * @author Alexander Nozik
 */
public interface EnvelopeType {

    ServiceLoader<EnvelopeType> loader = ServiceLoader.load(EnvelopeType.class);

    static EnvelopeType resolve(int code) {
        return StreamSupport.stream(loader.spliterator(), false)
                .filter(it -> it.getCode() == code).findFirst().orElse(null);
    }

    static EnvelopeType resolve(String name) {
        return StreamSupport.stream(loader.spliterator(), false)
                .filter(it -> Objects.equals(it.getName(), name)).findFirst().orElse(null);
    }

    int getCode();

    String getName();

    String description();

    default Map<String, Value> defaultProperties() {
        return Collections.emptyMap();
    }

    EnvelopeReader getReader();

    EnvelopeWriter getWriter();

    /**
     * True if metadata length auto detection is allowed
     *
     * @return
     */
    boolean infiniteMetaAllowed();

    /**
     * True if data length auto detection is allowed
     *
     * @return
     */
    boolean infiniteDataAllowed();

}
