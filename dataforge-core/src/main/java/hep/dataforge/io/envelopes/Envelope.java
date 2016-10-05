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

import hep.dataforge.data.binary.Binary;
import hep.dataforge.meta.Annotated;
import hep.dataforge.values.Value;

import java.util.Map;

/**
 * The message is a pack that can include three principal parts:
 * <ul>
 * <li>Envelope properties</li>
 * <li>Envelope meta-data</li>
 * <li>binary data</li>
 * </ul>
 *
 * @author Alexander Nozik
 */
public interface Envelope extends Annotated {

    //Constants
    /**
     * The type of the envelope
     */
    String TYPE_KEY = "type";

    /**
     * Version of envelope
     */
    String VERSION_KEY = "version";

    String OPT_KEY = "opt";
    String META_TYPE_KEY = "metaType";
    String META_ENCODING_KEY = "metaEncoding";
    String META_LENGTH_KEY = "metaLength";
    String DATA_TYPE_KEY = "dataType";
    String DATA_LENGTH_KEY = "dataLength";

    Map<String, Value> getProperties();

    /**
     * Read data into buffer. This operation could take a lot of time so be
     * careful when performing it synchronously
     *
     * @return
     */
    Binary getData();

}
