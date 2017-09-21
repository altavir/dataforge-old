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

import hep.dataforge.data.Data;
import hep.dataforge.data.binary.Binary;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

/**
 * The message is a pack that can include two principal parts:
 * <ul>
 * <li>Envelope meta-data</li>
 * <li>binary data</li>
 * </ul>
 *
 * @author Alexander Nozik
 */
@NodeDef(name = "@envelope", info = "An optional envelope service info node")
@ValueDef(name = "@envelope.type", info = "Type of the envelope content")
@ValueDef(name = "@envelope.dataType", info = "Type of the envelope data encoding")
@ValueDef(name = "@envelope.description", info = "Description of the envelope content")
@ValueDef(name = "@envelope.time", type = ValueType.TIME, info = "Time of envelope creation")
public interface Envelope extends Metoid {
    /**
     * Property keys
     */
    String TYPE_KEY = "type";
    String META_TYPE_KEY = "metaType";
    String META_LENGTH_KEY = "metaLength";
    String DATA_LENGTH_KEY = "dataLength";

    /**
     * Meta part of the envelope
     *
     * @return
     */
    @Override
    Meta meta();

    /**
     * Read data into buffer. This operation could take a lot of time so be
     * careful when performing it synchronously
     *
     * @return
     */
    Binary getData();

    default boolean hasMeta() {
        return !meta().isEmpty();
    }

    default boolean hasData() {
        try {
            return getData().size() > 0;
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Failed to estimate data size in the envelope", e);
            return false;
        }
    }

    /**
     * The purpose of the envelope
     *
     * @return
     */
    default Optional<String> getType() {
        return meta().optValue("@envelope.type").map(Value::stringValue);
    }

    /**
     * The type of data encoding
     *
     * @return
     */
    default Optional<String> getDataType() {
        return meta().optValue("@envelope.dataType").map(Value::stringValue);
    }

    /**
     * Textual user friendly description
     *
     * @return
     */
    default String getDescription() {
        return meta().getString("@envelope.description", "");
    }

    /**
     * Time of creation of the envelope
     *
     * @return
     */
    default Optional<Instant> getTime() {
        return meta().optValue("@envelope.time").map(Value::timeValue);
    }

    /**
     * Transform Envelope to Lazy data using given transformation.
     * In case transformation failed an exception will be thrown in call site.
     *
     * @param type
     * @param transform
     * @param <T>
     * @return
     */
    default <T> Data<T> map(Class<T> type, Function<Binary, T> transform) {
        return Data.generate(type, meta(), () -> transform.apply(getData()));
    }
}
