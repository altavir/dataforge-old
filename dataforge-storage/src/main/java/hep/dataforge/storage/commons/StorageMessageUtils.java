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
package hep.dataforge.storage.commons;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import java.nio.ByteBuffer;

/**
 * A delegate to evaluate messages for loaders
 *
 * @author Alexander Nozik
 */
public class StorageMessageUtils {

    public static final String ACTION_KEY = "action";
    
    public static final String QUERY_ELEMENT = "query";
    

    //Message operations
    public static final String PUSH_OPERATION = "push";
    public static final String PULL_OPERATION = "pull";

    /**
     * Create a default 'OK' response for push request
     *
     * @param request
     * @return
     */
    public static Envelope confirmationResponse(Envelope request) {
        Meta meta = new MetaBuilder("response")
                .putValue("success", true)
                .build();

        return new EnvelopeBuilder(request)
                .setMeta(meta)
                .build();

    }

    public static Envelope exceptionResponse(Envelope request, Throwable... exceptions) {
        MetaBuilder meta = new MetaBuilder("response")
                .putValue("success", false);

        for(Throwable exception: exceptions){
            MetaBuilder ex = new MetaBuilder("error")
                    .putValue("type", exception.getClass().getTypeName())
                    .putValue("message", exception.getMessage());
            meta.putNode(ex);
        }
        
        return new EnvelopeBuilder(request)
                .setMeta(meta.build())
                .build();
    }    
    
    public static Envelope response(Envelope request, Meta response, ByteBuffer data){
        throw new UnsupportedOperationException();
    }

}
