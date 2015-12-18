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
public class EnvelopeTypeLibrary extends PropertyLib<EnvelopeType> {
    public static EnvelopeType DEFAULT_ENVELOPE_TYPE = new DefaultEnvelopeType();
    
    private static final EnvelopeTypeLibrary instance = new EnvelopeTypeLibrary();
    
    public static final EnvelopeTypeLibrary instance(){
        return instance;
    }
    
    @Override
    public EnvelopeType getDefault() {
        return DEFAULT_ENVELOPE_TYPE;
    }

    private EnvelopeTypeLibrary() {
        putComposite(33, "default", DEFAULT_ENVELOPE_TYPE);
    }
    
    
    
    
    
}
