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
import java.util.Map;

/**
 *
 * @author Alexander Nozik
 */
public class SimpleEnvelope implements Envelope {
    protected final Map<String, Value> properties;
    protected final Meta meta;
    protected final ByteBuffer data;

    public SimpleEnvelope(Map<String, Value> properties, Meta meta, ByteBuffer data) {
        this.properties = properties;
        this.meta = meta;
        this.data = data;
    }

    @Override
    public Map<String, Value> getProperties() {
        return properties;
    }

    @Override
    public ByteBuffer getData() {
        return data;
    }

    @Override
    public Meta meta() {
        return meta;
    }
    
}
