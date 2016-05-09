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

/**
 *
 * @author darksnake
 */
public class DefaultEnvelopeType implements EnvelopeType<Envelope> {
    /**
     * The set of symbols that separates tag from metadata and data
     */
    public static final byte[] SEPARATOR = {'\r', '\n'};

    @Override
    public String description() {
        return "Standard envelope type. Meta and data autodetectio are not supported. Tag is mandatory.";
    }

    @Override
    public short getCode() {
        return 0x0000;
    }

    @Override
    public String getName() {
        return "default";
    }
    
    

    @Override
    public EnvelopeReader<Envelope> getReader() {
        return DefaultEnvelopeReader.instance;
    }

    @Override
    public EnvelopeWriter<Envelope> getWriter() {
        return DefaultEnvelopeWriter.instance;
    }

    @Override
    public boolean infiniteDataAllowed() {
        return false;
    }

    @Override
    public boolean infiniteMetaAllowed() {
        return false;
    }

}
