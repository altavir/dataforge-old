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

import java.util.Map;

/**
 * @author darksnake
 */
public class DefaultEnvelopeType implements EnvelopeType {

    public static DefaultEnvelopeType instance = new DefaultEnvelopeType();

    public static int DEFAULT_ENVELOPE_TYPE = 0x44463032;

    /**
     * The set of symbols that separates tag from metadata and data
     */
    public static final byte[] SEPARATOR = {'\r', '\n'};

    @Override
    public String description() {
        return "Standard envelope type. Meta and data end auto detection are not supported. Tag is mandatory.";
    }

    @Override
    public int getCode() {
        return DEFAULT_ENVELOPE_TYPE;
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public EnvelopeReader getReader(Map<String, String> properties) {
        return DefaultEnvelopeReader.INSTANCE;
    }

    @Override
    public EnvelopeWriter getWriter(Map<String, String> properties) {
        return new DefaultEnvelopeWriter(this,MetaType.resolve(properties));
    }

    /**
     * True if metadata length auto detection is allowed
     *
     * @return
     */
    public boolean infiniteDataAllowed() {
        return false;
    }

    /**
     * True if data length auto detection is allowed
     *
     * @return
     */
    public boolean infiniteMetaAllowed() {
        return false;
    }

}
