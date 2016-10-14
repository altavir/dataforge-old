package hep.dataforge.utils;

import hep.dataforge.values.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by darksnake on 14-Oct-16.
 */
public class DateTimeUtils {
    public static Instant now() {
        return LocalDateTime.now().toInstant(ZoneOffset.UTC);
    }

    public static Value nowValue() {
        return Value.of(now());
    }
}
