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
package hep.dataforge.context;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import hep.dataforge.actions.ActionManager;
import hep.dataforge.actions.RunConfigAction;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.BasicIOManager;
import hep.dataforge.io.IOManager;
import hep.dataforge.io.reports.ReportEntry;
import hep.dataforge.tables.ReadPointSetAction;
import hep.dataforge.tables.TransformTableAction;
import hep.dataforge.values.Value;
import java.io.File;
import java.io.PrintWriter;
import java.util.Locale;
import org.slf4j.LoggerFactory;

/**
 * Глобальный контекст. Хранит не только глобальные настройки, но и
 * контроллирует загрузку и выгрузку модулей
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class GlobalContext extends Context {

    private static final GlobalContext instance = new GlobalContext();

    public static GlobalContext instance() {
        return instance;
    }

    public static File getFile(String path) {
        return instance.io().getFile(path);
    }

    public static PrintWriter out() {
        return new PrintWriter(instance.io().out());
    }

    private GlobalContext() {
        super("df");
        Locale.setDefault(Locale.US);
        ActionManager actions = new ActionManager();
        loadPlugin(actions);
        actions.registerAction(TransformTableAction.class);
        actions.registerAction(ReadPointSetAction.class);
        actions.registerAction(RunConfigAction.class);
        this.processManager = new ProcessManager();
        this.processManager.setContext(this);        
    }

    @Override
    public void setIO(IOManager io) {
        super.setIO(io);
        //redirect all logging output to new ioManager
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        //redirect output to given outputstream
        root.detachAndStopAllAppenders();
        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setContext(root.getLoggerContext());
        appender.setOutputStream(io.out());
        appender.start();
        root.addAppender(appender);
    }

    @Override
    public IOManager io() {
        if (this.io == null) {
            setIO(new BasicIOManager());
            getReport().addReportListener((ReportEntry t) -> {
                System.out.println(t.toString());
            });
        }
        return super.io();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Context getParent() {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @param path
     * @return
     */
    @Override
    public Value getValue(String path) {
        if (properties.containsKey(path)) {
            return properties.get(path);
        } else {
            throw new NameNotFoundException(path);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param path
     * @return
     */
    @Override
    public boolean hasValue(String path) {
        return properties.containsKey(path);
    }

}
