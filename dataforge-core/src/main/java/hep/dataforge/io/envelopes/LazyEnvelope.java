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

import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The envelope that does not store data part in memory but reads it on demand.
 *
 * @author darksnake
 */
public class LazyEnvelope implements Envelope {

    private final Map<String, Value> properties;
    private final Meta meta;
    private Supplier<ByteBuffer> supplier;
    private ByteBuffer data = null;

    public LazyEnvelope(Map<String, Value> properties, Meta meta, Supplier<ByteBuffer> supplier) {
        this.properties = new HashMap<>(properties);
        this.meta = meta;
        this.supplier = supplier;
    }

    @Override
    public Meta meta() {
        return meta;
    }

    /**
     * Calculate data buffer if it is not already calculated and return result.
     *
     * @return
     */
    @Override
    public ByteBuffer getData() {
        if (data == null) {
            data = getDataSupplier().get();
            //invalidating supplier
            supplier = null;
        }
        return data;
    }

    /**
     * Return supplier of data for lazy calculation. The supplier is supposed to
     *
     * @return
     */
    private Supplier<ByteBuffer> getDataSupplier() {
        return supplier;
    }

    @Override
    public Map<String, Value> getProperties() {
        return properties;
    }

}
