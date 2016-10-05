package hep.dataforge.storage.servlet;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.Responder;
import ratpack.form.Form;
import ratpack.handling.Context;
import ratpack.handling.Handler;

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

            String metaString = request.get("meta");
        });
    }

    @Override
    public Envelope respond(Envelope message) {
        return respond(respond(message));
    }
}
