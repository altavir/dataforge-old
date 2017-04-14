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
package hep.dataforge.io.reports;

import hep.dataforge.context.Global;
import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.names.Named;
import hep.dataforge.utils.ReferenceRegistry;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * A in-memory log that can store a finite number of entries. The difference between logger events and log is that log
 * is usually part the part of the analysis result an should be preserved.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Log implements Loggable, Named {

    private static int MAX_LOG_SIZE = 1000;
    private final String name;
    private final ReferenceRegistry<Consumer<LogEntry>> listeners = new ReferenceRegistry<>();
    protected ConcurrentLinkedQueue<LogEntry> entries = new ConcurrentLinkedQueue<>();
    private Loggable parent;

    public Log(String name, Loggable parent) {
        if (name == null || name.isEmpty()) {
            throw new AnonymousNotAlowedException();
        }
        this.name = name;
        this.parent = parent;
    }

    public Log(String name) {
        this(name, Global.instance());
    }

    public void setParent(Loggable parent) {
        this.parent = parent;
    }

    protected int getMaxLogSize() {
        return MAX_LOG_SIZE;
    }

    @Override
    public void report(LogEntry entry) {
        entries.add(entry);
        if (entries.size() >= getMaxLogSize()) {
            entries.poll();// Ограничение на размер лога
//            getLogger().warn("Log at maximum capacity!");
        }
        listeners.forEach((Consumer<LogEntry> listener) -> {
            listener.accept(entry);
        });

        if (getParent() != null) {
            LogEntry newEntry = pushTrace(entry, getName());
            getParent().report(newEntry);
        }
    }

    /**
     * Add a weak report listener to this report
     *
     * @param logListener
     */
    public void addListener(Consumer<LogEntry> logListener) {
        this.listeners.add(logListener, true);
    }

    private LogEntry pushTrace(LogEntry entry, String toTrace) {
        return new LogEntry(entry, toTrace);
    }

    public void clear() {
        entries.clear();
    }

    public Loggable getParent() {
        return parent;
    }

    public void print(PrintWriter out) {
        out.println();
        entries.stream().forEach((entry) -> {
            out.println(entry.toString());
        });
        out.println();
        out.flush();
    }

    public Log getLog() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void report(String str, Object... parameters) {
        LogEntry entry = new LogEntry(MessageFormatter.arrayFormat(str, parameters).getMessage());
        Log.this.report(entry);
    }

    @Override
    public void reportError(String str, Object... parameters) {
        Log.this.report(new LogEntry("[ERROR] " + MessageFormatter.arrayFormat(str, parameters).getMessage()));
    }
}
