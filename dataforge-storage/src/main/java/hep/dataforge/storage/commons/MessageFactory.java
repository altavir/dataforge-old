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
import static hep.dataforge.io.envelopes.Envelope.*;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.envelopes.Tag;
import static hep.dataforge.storage.commons.EnvelopeCodes.DATAFORGE_MESSAGE_ENVELOPE_CODE;

/**
 * A factory for messages with fixed format
 * ({@code DATAFORGE_MESSAGE_ENVELOPE_CODE}) and meta properties
 *
 * @author Alexander Nozik
 */
public class MessageFactory {

    public static final String MESSAGE_TYPE_KEY = "type";
    public static final String RESPONSE_SUCCESS_KEY = "success";
    public static final String RESPONSE_TYPE_SUFFIX = ".response";
    public static final String ERROR_RESPONSE_TYPE = "error";

    /**
     * Generate a response base with the same meta parameters (type, encoding)
     * as in request and modified message type if it is present
     *
     * @param request
     * @return
     */
    public EnvelopeBuilder responseBase(Envelope request) {
        EnvelopeBuilder res = new EnvelopeBuilder()
                .setEnvelopeType(DATAFORGE_MESSAGE_ENVELOPE_CODE)
                .setMetaEncoding(request.getProperties().get(META_ENCODING_KEY).stringValue())
                .setMetaType(request.getProperties().get(META_TYPE_KEY).stringValue());
        String type = request.meta().getString(MESSAGE_TYPE_KEY, "");
        if (!type.isEmpty()) {
            res.putMetaValue(MESSAGE_TYPE_KEY, type + RESPONSE_TYPE_SUFFIX);
        }
        return res;
    }

    /**
     * Response base with given type (could be null) and default meta parameters
     *
     * @param type
     * @return
     */
    public EnvelopeBuilder responseBase(String type) {
        EnvelopeBuilder res = new EnvelopeBuilder()
                .setEnvelopeType(DATAFORGE_MESSAGE_ENVELOPE_CODE)
                .setMetaEncoding(metaEncoding())
                .setMetaType(metaType());
        if (type != null && !type.isEmpty()) {
            if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
                type += RESPONSE_TYPE_SUFFIX;
            }
            res.putMetaValue(MESSAGE_TYPE_KEY, type);
        }
        return res;
    }

    /**
     * Request base with given type (could be null) and default meta parameters
     *
     * @param type
     * @return
     */
    public EnvelopeBuilder requestBase(String type) {
        EnvelopeBuilder res = new EnvelopeBuilder()
                .setEnvelopeType(DATAFORGE_MESSAGE_ENVELOPE_CODE)
                .setMetaEncoding(metaEncoding())
                .setMetaType(metaType());
        if (type != null && !type.isEmpty()) {

            res.putMetaValue(MESSAGE_TYPE_KEY, type);
        }
        return res;
    }

    /**
     * Terminator envelope that should be sent to close current connection
     *
     * @return
     */
    public Envelope terminator() {
        return new EnvelopeBuilder()
                .setEnvelopeType(EnvelopeCodes.DATAFORGE_MESSAGE_ENVELOPE_CODE)
                .setDataType(EnvelopeCodes.MESSAGE_TERMINATOR_CODE)
                .build();
    }

    /**
     * An empty confirmation response without meta and data
     *
     * @param type
     * @return
     */
    public Envelope okResponse(String type) {
        return okResponseBase(type, false, false).build();
    }

    public EnvelopeBuilder okResponseBase(Envelope request, boolean hasMeta, boolean hasData) {
        EnvelopeBuilder res = new EnvelopeBuilder()
                .setEnvelopeType(DATAFORGE_MESSAGE_ENVELOPE_CODE)
                .setMetaEncoding(request.getProperties().get(META_ENCODING_KEY).stringValue())
                .setMetaType(request.getProperties().get(META_TYPE_KEY).stringValue())
                .setDataType(okResponseCode(hasMeta, hasData));
        String type = request.meta().getString(MESSAGE_TYPE_KEY, "");
        if (type != null && !type.isEmpty()) {
            if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
                type += RESPONSE_TYPE_SUFFIX;
            }
            res.putMetaValue(MESSAGE_TYPE_KEY, type);
        }
        if (hasMeta) {
            res.putMetaValue(RESPONSE_SUCCESS_KEY, true);
        }

        return res;
    }

    /**
     * Confirmation response base
     *
     * @param type
     * @param hasMeta
     * @param hasData
     * @return
     */
    public EnvelopeBuilder okResponseBase(String type, boolean hasMeta, boolean hasData) {
        EnvelopeBuilder res = new EnvelopeBuilder()
                .setEnvelopeType(DATAFORGE_MESSAGE_ENVELOPE_CODE)
                .setMetaEncoding(metaEncoding())
                .setMetaType(metaType())
                .setDataType(okResponseCode(hasMeta, hasData));
        if (type != null && !type.isEmpty()) {
            if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
                type += RESPONSE_TYPE_SUFFIX;
            }
            res.putMetaValue(MESSAGE_TYPE_KEY, type);
        }
        if (hasMeta) {
            res.putMetaValue(RESPONSE_SUCCESS_KEY, true);
        }

        return res;
    }

    protected int okResponseCode(boolean hasMeta, boolean hasData) {
        int res = EnvelopeCodes.MESSAGE_CONFIRM_CODE;
        if (hasMeta) {
            res = res | EnvelopeCodes.MESSAGE_HAS_META_FLAG;
        }
        if (hasMeta) {
            res = res | EnvelopeCodes.MESSAGE_HAS_DATA_FLAG;
        }
        return res;
    }

    protected int failResponseCode() {
        return EnvelopeCodes.MESSAGE_FAIL_CODE;
    }

    protected String metaEncoding() {
        return "UTF-8";
    }

    protected String metaType() {
        return "JSON";
    }

    /**
     * A error response base with given exceptions
     *
     * @param type
     * @param errors
     * @return
     */
    public EnvelopeBuilder errorResponseBase(String type, Throwable... errors) {
        if (type == null || type.isEmpty()) {
            type = ERROR_RESPONSE_TYPE;
        }
        if (!type.endsWith(RESPONSE_TYPE_SUFFIX)) {
            type += RESPONSE_TYPE_SUFFIX;
        }
        EnvelopeBuilder builder = responseBase(type)
                .setDataType(failResponseCode());
        for (Throwable err : errors) {
            builder.putMetaNode(StorageUtils.getErrorMeta(err));
        }
        return builder.putMetaValue(RESPONSE_SUCCESS_KEY, false);
    }

    public EnvelopeBuilder errorResponseBase(Envelope request, Throwable... errors) {
        return errorResponseBase(request.meta().getString(MESSAGE_TYPE_KEY, ""), errors);
    }

}
