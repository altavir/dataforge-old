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
import hep.dataforge.storage.commons.EnvelopeCodes;

/**
 *
 * @author darksnake
 */
public class FileStorageEnvelopeType extends DefaultEnvelopeType {

    @Override
    public String description() {
        return "DataForge file storage envelope";
    }

    @Override
    public String getName() {
        return "hep.dataforge.storage";
    }

    @Override
    public int getCode() {
        return EnvelopeCodes.DATAFORGE_ENVELOPE | EnvelopeCodes.DATAFORGE_STORAGE_ENVELOPE_CODE;
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
