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
package hep.dataforge.storage.filestorage;

import hep.dataforge.io.envelopes.DefaultEnvelopeType;
import hep.dataforge.io.envelopes.DefaultEnvelopeWriter;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.storage.commons.JSONMetaType;

/**
 * An envelope type for storage binaries. Infinite data allowed
 * @author darksnake
 */
public class FileStorageEnvelopeType extends DefaultEnvelopeType {
    public static final String FILE_STORAGE_ENVELOPE_TYPE = "storage";

    /**
     * Check that declared envelope content type is a storage or empty
     * @param envelope
     * @return
     */
    public static boolean validate(Envelope envelope){
        return envelope.getType().orElse(FILE_STORAGE_ENVELOPE_TYPE).equals(FILE_STORAGE_ENVELOPE_TYPE);
    }

    public static boolean validate(Envelope envelope, String loaderType){
        return validate(envelope)&& envelope.meta().getString("type","").equals(loaderType);
    }

    @Override
    public String description() {
        return "DataForge file storage envelope";
    }

    @Override
    public String getName() {
        return FILE_STORAGE_ENVELOPE_TYPE;
    }

    @Override
    public DefaultEnvelopeWriter getWriter() {
        return new DefaultEnvelopeWriter(this, JSONMetaType.instance);
    }

    @Override
    public int getCode() {
        return 0x44465354;//DFST
    }

    @Override
    public boolean infiniteMetaAllowed() {
        return false;
    }

    @Override
    public boolean infiniteDataAllowed() {
        return true;
    }

}
