package hep.dataforge.server.storage;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeBuilder;
import hep.dataforge.io.envelopes.MetaType;
import hep.dataforge.io.messages.Responder;
import hep.dataforge.meta.Meta;
import org.jetbrains.annotations.NotNull;
import ratpack.form.Form;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.util.concurrent.CompletableFuture;

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
            String metaType = request.get(Envelope.META_TYPE_PROPERTY);
            String metaString = request.get("meta");

            EnvelopeBuilder builder = new EnvelopeBuilder();

            Meta meta = MetaType.Companion.resolve(metaType).getReader().readString(metaString);
            byte[] data = request.file("data").getBytes();

            builder.setMeta(meta);
            builder.setData(data);

            Envelope response = respond(builder.build());
            ctx.render(new JSONMetaWriter().writeString(response.getMeta()));
        });
    }

    @NotNull
    @Override
    public Envelope respond(@NotNull Envelope message) {
        return responder.respond(message);
    }

    @NotNull
    @Override
    public CompletableFuture<Envelope> respondInFuture(@NotNull Envelope message) {
        return CompletableFuture.supplyAsync(() -> respond(message));
    }
}
