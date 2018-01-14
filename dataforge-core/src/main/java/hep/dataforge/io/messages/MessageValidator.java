package hep.dataforge.io.messages;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;

/**
 * A validator checking incoming messages. It colud be used for security or bug checks
 * Created by darksnake on 12-Oct-16.
 */
public interface MessageValidator {
    String IS_VALID_KEY = "isValid";
    String MESSAGE_KEY = "message";

    Meta validate(Envelope message);

    default boolean isValid(Envelope message) {
        return validate(message).getBoolean(IS_VALID_KEY);
    }

    static Meta valid() {
        return new MetaBuilder("validationResult").putValue(IS_VALID_KEY, true).build();
    }

    static Meta invalid(String... message) {
        return new MetaBuilder("validationResult")
                .putValue(IS_VALID_KEY, false)
                .putValue(MESSAGE_KEY, message)
                .build();
    }
}
