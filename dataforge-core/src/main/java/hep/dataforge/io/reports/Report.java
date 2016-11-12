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

import hep.dataforge.context.GlobalContext;
import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.names.Named;
import hep.dataforge.utils.ReferenceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Лог организован таким образом, что он добавляет каждую строчкку в себя и по
 * цепочке во все родительские. Когда надо напечатать что-то можно просто
 * выбрать уровень лога.
 *
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Report implements Reportable, Named {

    private static int MAX_LOG_SIZE = 1000;
    private final String name;
    private final ReferenceRegistry<Consumer<ReportEntry>> listeners = new ReferenceRegistry<>();
    protected ConcurrentLinkedQueue<ReportEntry> entries = new ConcurrentLinkedQueue<>();
    private Reportable parent;
    private Logger logger;

    public Report(String name, Reportable parent) {
        if (name == null || name.isEmpty()) {
            throw new AnonymousNotAlowedException();
        }
        this.name = name;
        this.parent = parent;
    }

    public Report(String name) {
        this(name, GlobalContext.instance());
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = (Logger) LoggerFactory.getLogger(getName());
        }
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected int getMaxLogSize() {
        return MAX_LOG_SIZE;
    }

    @Override
    public void report(ReportEntry entry) {
        entries.add(entry);
        if (entries.size() >= getMaxLogSize()) {
            entries.poll();// Ограничение на размер лога
//            getLogger().warn("Log at maximum capacity!");
        }
        listeners.forEach((Consumer<ReportEntry> listener) -> {
            listener.accept(entry);
        });

        if (getParent() != null) {
            ReportEntry newEntry = pushTrace(entry, getName());
            getParent().report(newEntry);
        }
    }

    /**
     * Add a weak report listener to this report
     *
     * @param logListener
     */
    public void addReportListener(Consumer<ReportEntry> logListener) {
        this.listeners.add(logListener, true);
    }

    private ReportEntry pushTrace(ReportEntry entry, String toTrace) {
        return new ReportEntry(entry, toTrace);
    }

    public void clear() {
        entries.clear();
    }

    public Reportable getParent() {
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

    @Override
    public Report getReport() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void report(String str, Object... parameters) {
        ReportEntry entry = new ReportEntry(MessageFormatter.arrayFormat(str, parameters).getMessage());
        Report.this.report(entry);
    }

    @Override
    public void reportError(String str, Object... parameters) {
        Report.this.report(new ReportEntry("[ERROR] " + MessageFormatter.arrayFormat(str, parameters).getMessage()));
        getLogger().error(str, parameters);
    }
}
