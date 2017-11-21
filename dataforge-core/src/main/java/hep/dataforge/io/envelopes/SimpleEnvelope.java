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
import hep.dataforge.meta.Meta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The simplest in-memory envelope
 *
 * @author Alexander Nozik
 */
public class SimpleEnvelope implements Envelope {
    protected Meta meta;
    protected Binary data;

    public SimpleEnvelope() {
    }

    public SimpleEnvelope(Meta meta, Binary data) {
        this.meta = meta;
        this.data = data;
    }

    @Override
    public Binary getData() {
        return data == null ? Binary.EMPTY : data;
    }

    @Override
    public Meta getMeta() {
        return meta == null ? Meta.empty() : meta;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        DefaultEnvelopeType.instance.getWriter().write(out, this);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Envelope envelope = DefaultEnvelopeType.instance.getReader().read(in);
        this.meta = envelope.getMeta();
        this.data = envelope.getData();
    }

}
