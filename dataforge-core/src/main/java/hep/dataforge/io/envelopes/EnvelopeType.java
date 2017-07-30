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

import hep.dataforge.context.Global;
import hep.dataforge.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardOpenOption.READ;

/**
 * Envelope io format description
 *
 * @author Alexander Nozik
 */
public interface EnvelopeType {

    ServiceLoader<EnvelopeType> loader = ServiceLoader.load(EnvelopeType.class);

    /**
     * Infer envelope type from file reading only first line (ignoring empty and sha-bang)
     *
     * @param path
     * @return
     */
    static Optional<EnvelopeType> infer(Path path) {
        try {
            return IOUtils.nextLine(
                    Files.newInputStream(path,READ),
                    "ASCII",
                    line -> line.isEmpty()|| (line.startsWith("#!") && ! line.endsWith("#!"))
            ).flatMap(header -> {
                if (header.startsWith("#~") || (header.startsWith("#!") && header.trim().endsWith("!#"))) {
                    return Optional.of(DefaultEnvelopeType.instance);
                } else if (header.startsWith("#~DFTL")) {
                    return Optional.of(TaglessEnvelopeType.instance);
                } else {
                    return Optional.empty();
                }
            });
        } catch (Exception ex) {
            LoggerFactory.getLogger(EnvelopeType.class).warn("Could not infer envelope type of file {} due to exception: {}", path, ex);
            return Optional.empty();
        }
    }

    static EnvelopeType resolve(int code) {
        synchronized (Global.instance()) {
            return StreamSupport.stream(loader.spliterator(), false)
                    .filter(it -> it.getCode() == code).findFirst().orElse(null);
        }
    }

    static EnvelopeType resolve(String name) {
        synchronized (Global.instance()) {
            return StreamSupport.stream(loader.spliterator(), false)
                    .filter(it -> Objects.equals(it.getName(), name)).findFirst().orElse(null);
        }
    }

    int getCode();

    String getName();

    String description();

    /**
     * Get reader with properties override
     *
     * @param properties
     * @return
     */
    EnvelopeReader getReader(Map<String, String> properties);

    default EnvelopeReader getReader() {
        return getReader(Collections.emptyMap());
    }

    /**
     * Get writer with properties override
     *
     * @param properties
     * @return
     */
    EnvelopeWriter getWriter(Map<String, String> properties);

    default EnvelopeWriter getWriter() {
        return getWriter(Collections.emptyMap());
    }
}
