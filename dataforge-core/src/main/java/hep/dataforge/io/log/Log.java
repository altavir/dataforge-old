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

import ch.qos.logback.classic.Logger;
import hep.dataforge.content.Named;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.utils.ReferenceRegistry;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Лог организован таким образом, что он добавляет каждую строчкку в себя и по
 * цепочке во все родительские. Когда надо напечатать что-то можно просто
 * выбрать уровень лога.
 *
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Log implements Logable, Named {

    private static int MAX_LOG_SIZE = 1000;

    protected ConcurrentLinkedQueue<LogEntry> log = new ConcurrentLinkedQueue<>();
    private Logable parent;
    private Logger logger;
    private final String name;
    private final ReferenceRegistry<Consumer<LogEntry>> logListeners = new ReferenceRegistry<>();

    public Log(String name, Logable parent) {
        if (name == null || name.isEmpty()) {
            throw new AnonymousNotAlowedException();
        }
        this.name = name;
        this.parent = parent;
        this.logger = (Logger) LoggerFactory.getLogger(name);
    }

    public Log(String name) {
        this(name, GlobalContext.instance());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    protected int getMaxLogSize() {
        return MAX_LOG_SIZE;
    }

    @Override
    public void log(LogEntry entry) {
        log.add(entry);
        if (log.size() >= getMaxLogSize()) {
            log.poll();// Ограничение на размер лога
            logger.warn("Log at maximum capacity!");
        }
        logListeners.forEach((Consumer<LogEntry> listener) -> {
            listener.accept(entry);
        });

        if (getParent() != null) {
            LogEntry newEntry = pushTrace(entry, getName());
            getParent().log(newEntry);
        }

    }

    /**
     * Add a weak log listener to this  log
     *
     * @param logListener
     */
    public void addLogListener(Consumer<LogEntry> logListener) {
        this.logListeners.add(logListener);
    }

    private LogEntry pushTrace(LogEntry entry, String toTrace) {
        return new LogEntry(entry, toTrace);
    }

    public void clear() {
        log.clear();
    }

    public Logable getParent() {
        return parent;
    }

    public void print(PrintWriter out) {
        out.println();
        log.stream().forEach((entry) -> {
            out.println(entry.toString());
        });
        out.println();
        out.flush();
    }

    @Override
    public Log getLog() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void log(String str, Object... parameters) {
        LogEntry entry = new LogEntry(MessageFormatter.arrayFormat(str, parameters).getMessage());
        log(entry);
    }

    @Override
    public void logError(String str, Object... parameters) {
        this.log.add(new LogEntry("[ERROR] " + MessageFormatter.arrayFormat(str, parameters).getMessage()));
        logger.error(str, parameters);
    }
}
