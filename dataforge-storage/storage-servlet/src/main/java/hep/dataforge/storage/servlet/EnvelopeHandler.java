package hep.dataforge.storage.servlet;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.envelopes.EnvelopePropertyCodes;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.commons.JSONMetaWriter;
import ratpack.form.Form;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.nio.charset.Charset;

/**
 * Created by darksnake on 05-Oct-16.
 */
public class EnvelopeHandler implements Handler, Responder {

    private final Responder responder;

    public EnvelopeHandler(Responder responder) {
        this.responder = responder;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.parse(Form.class).then(request -> {
            String metaType = request.get(Envelope.META_TYPE_KEY);
            String metaEncoding = request.get(Envelope.META_ENCODING_KEY);
            String metaString = request.get("meta");

            EnvelopeBuilder builder = new EnvelopeBuilder();

            Meta meta = EnvelopePropertyCodes.getMetaType(metaType).getReader().readString(metaString, Charset.forName(metaEncoding));
            byte[] data = request.file("data").getBytes();

            builder.setMeta(meta);
            builder.setData(data);
            request.forEach((key, value) -> {
                if (!key.equals("meta")) {
                    builder.setProperty(key, value);
                }
            });
            Envelope response = respond(builder.build());
            ctx.render(new JSONMetaWriter().writeString(response.meta()));
        });
    }

    @Override
    public Envelope respond(Envelope message) {
        return responder.respond(message);
    }
}
