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
import hep.dataforge.actions.ActionManager;
import hep.dataforge.actions.RunConfigAction;
import hep.dataforge.computation.TaskManager;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.BasicIOManager;
import hep.dataforge.io.IOManager;
import hep.dataforge.io.reports.ReportEntry;
import hep.dataforge.tables.ReadPointSetAction;
import hep.dataforge.tables.TransformTableAction;
import hep.dataforge.values.Value;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Глобальный контекст. Хранит не только глобальные настройки, но и
 * контроллирует загрузку и выгрузку модулей
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class GlobalContext extends Context {

    private static final GlobalContext instance = new GlobalContext();
    private static final Set<Context> contexts = new HashSet<>();
    private static final ExecutorService dispatchThreadExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread res = new Thread(r, "DF_DISPATCH");
//        res.setDaemon(false);
        res.setPriority(Thread.MAX_PRIORITY);
        return res;
    });

    private GlobalContext() {
        super("GLOBAL");
        Locale.setDefault(Locale.US);
        rootLog.setLogger((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME));
        ActionManager actions = new ActionManager();
        //TODO move to plugin
        pluginManager().loadPlugin(actions);
        actions.registerAction(TransformTableAction.class);
        actions.registerAction(ReadPointSetAction.class);
        actions.registerAction(RunConfigAction.class);
    }

    /**
     * A single thread executor for DataForge messages dispatch. No heavy calculations should be done on this thread
     *
     * @return
     */
    public static ExecutorService dispatchThreadExecutor() {
        return dispatchThreadExecutor;
    }

    public static GlobalContext instance() {
        return instance;
    }

    /**
     * Get previously registered context
     *
     * @param contextName
     * @return
     */
    public static Context getContext(String contextName) {
        return contexts.stream().filter(ctx -> ctx.getName().equals(contextName)).findFirst().orElse(null);
    }

    /**
     * Register a context to be retrieved later
     *
     * @param context
     */
    public static void registerContext(Context context) {
        contexts.add(context);
    }

    /**
     * Remove context from registry
     *
     * @param context
     */
    public static void unregisterContext(Context context) {
        contexts.remove(context);
    }

    public static File getFile(String path) {
        return instance.io().getFile(path);
    }

    public static PrintWriter out() {
        return new PrintWriter(instance.io().out());
    }

    @Override
    public void setIO(IOManager io) {
        super.setIO(io);
//        LoggerFactory.getLogger(getClass()).warn("Changing io for Global. Is not recommended.");
        //redirect all logging output to new ioManager
//        Logger root = getLogger();
//
//        //redirect output to given outputstream
////        root.detachAndStopAllAppenders();
//        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
//        appender.setContext(root.getLoggerContext());
//        appender.setOutputStream(io.out());
//        appender.start();
//        root.addAppender(appender);
    }

    @Override
    public TaskManager taskManager() {
        if (taskManager == null) {
            this.taskManager = new TaskManager();
            this.taskManager.setContext(this);
        }
        return super.taskManager();
    }

    @Override
    public IOManager io() {
        if (this.io == null) {
            setIO(new BasicIOManager());
            getReport().addReportListener((ReportEntry t) -> {
                System.out.println(t.toString());
            });
        }
        return io;
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

    /**
     * The global context independent temporary user directory. This directory
     * is used to store user configuration files. Never use it to store data.
     *
     * @return
     */
    public File getUserDirectory() {
        File userDir = new File(System.getProperty("user.home"));
        File dfUserDir = new File(userDir, ".dataforge");
        if (!dfUserDir.exists()) {
            dfUserDir.mkdir();
        }
        return dfUserDir;
    }

    /**
     * Closing all contexts
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        for (Context ctx : contexts) {
            ctx.close();
        }
        dispatchThreadExecutor.shutdown();
        super.close();
    }

}
