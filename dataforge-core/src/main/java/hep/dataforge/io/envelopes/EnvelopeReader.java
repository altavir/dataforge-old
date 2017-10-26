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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;

/**
 * interface for reading envelopes
 *
 * @author Alexander Nozik
 */
public interface EnvelopeReader {

    /**
     * Resolve envelope type and use it to read the file as envelope
     *
     * @param path
     * @return
     */
    static Envelope readFile(Path path) throws IOException {
        EnvelopeType type = EnvelopeType.infer(path).orElse(TaglessEnvelopeType.instance);
        return type.getReader().read(path);
    }

    /**
     * Read the whole envelope using internal properties reader.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    Envelope read(InputStream stream) throws IOException;

    default Envelope read(ByteBuffer buffer) throws IOException {
        return read(new ByteArrayInputStream(buffer.array()));
    }

    default Envelope read(Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file, READ)) {
            return read(stream);
        }
    }
}
