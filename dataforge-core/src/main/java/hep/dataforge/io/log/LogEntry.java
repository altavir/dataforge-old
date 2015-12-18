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
package hep.dataforge.io.log;

import static java.lang.String.format;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * LogEntry class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class LogEntry {

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private final List<String> sourceTrace = new ArrayList<>();
    private final String message;
    private final Instant time;

    public LogEntry(LogEntry entry, String traceAdd) {
        this.sourceTrace.addAll(entry.sourceTrace);
        if (traceAdd != null && !traceAdd.isEmpty()) {
            this.sourceTrace.add(0, traceAdd);
        }
        this.message = entry.message;
        this.time = entry.time;
    }

    public LogEntry(Instant time, String message) {
        this.time = time;
        this.message = message;
    }

    public LogEntry(String message) {
        this.time = Instant.now();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTime() {
        return time;
    }
    
    public String getTraceString(){
        return String.join(".", sourceTrace);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String toString() {
        String traceStr = getTraceString();
        if (traceStr.isEmpty()) {
            return format("(%s) %s", dateFormat.format(time), message);
        } else {
            return format("(%s) %s: %s", dateFormat.format(time), traceStr, message);
        }
    }
}
