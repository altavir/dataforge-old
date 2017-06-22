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
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;

/**
 * The message is a pack that can include three principal parts:
 * <ul>
 * <li>Envelope meta-data</li>
 * <li>binary data</li>
 * </ul>
 *
 * @author Alexander Nozik
 */
@NodeDef(name = "@envelope", info = "An optional envelope service info node")
@ValueDef(name = "@envelope.type", info = "Type of the envelope content")
@ValueDef(name = "@envelope.description", info = "Description of the envelope content")
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

    default String getContentType(String def){
        return meta().getString("@envelope.type",def);
    }

    default String getContentDescription(){
        return meta().getString("@envelope.description","");
    }

}
