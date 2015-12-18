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
package hep.dataforge.data;

import hep.dataforge.content.AbstractContent;
import hep.dataforge.meta.Meta;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>StreamData class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class StreamData extends AbstractContent implements BinaryData{

    private final InputStream stream;

    /**
     * <p>Constructor for StreamData.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param stream a {@link java.io.InputStream} object.
     */
    public StreamData(String name, InputStream stream) {
        super(name);
        this.stream = stream;
    }

    /**
     * <p>Constructor for StreamData.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param stream a {@link java.io.InputStream} object.
     */
    public StreamData(String name, Meta annotation, InputStream stream) {
        super(name, annotation);
        this.stream = stream;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getInputStream() throws IOException {
        return stream;
    }
}
