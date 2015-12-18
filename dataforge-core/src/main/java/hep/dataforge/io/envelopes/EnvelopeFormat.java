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

import hep.dataforge.exceptions.EnvelopeFormatException;
import hep.dataforge.values.Value;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * The interface defining envelope format. implementing objects must have an
 * empty constructor to be stored in library.
 *
 * @author darksnake
 * @param <T>
 */
public interface EnvelopeFormat<T extends Envelope> {

    T read(InputStream stream) throws IOException, EnvelopeFormatException;

    void writeToStream(OutputStream stream, T envelope) throws IOException;
    
    Map<String, Value> defaultProperties();
}
