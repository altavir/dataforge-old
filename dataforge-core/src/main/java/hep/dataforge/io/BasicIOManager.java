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
package hep.dataforge.io;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.names.Name;

import java.io.*;

/**
 * <p>
 * BasicIOManager class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@PluginDef(name = "io", group = "hep.dataforge", info = "Basic input and output plugin")
public class BasicIOManager extends BasicPlugin implements IOManager {

    private OutputStream out;
    private InputStream in;

    public BasicIOManager() {
    }

    public BasicIOManager(OutputStream out) {
        this.out = out;
    }

    public BasicIOManager(InputStream in, OutputStream out) {
        this.out = out;
        this.in = in;
    }

    /**
     * Create logger appender for this manager
     *
     * @return
     */
    public Appender<ILoggingEvent> createLoggerAppender() {
        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setOutputStream(out());
        return appender;
    }

    protected void addLoggerAppender(Logger logger) {
        LoggerContext loggerContext = logger.getLoggerContext();
        Appender<ILoggingEvent> appender = createLoggerAppender();
        appender.setName(LOGGER_APPENDER_NAME);
        appender.setContext(loggerContext);
        appender.start();
        logger.addAppender(appender);
    }

    protected void removeLoggerAppender(Logger logger) {
        Appender<ILoggingEvent> app = logger.getAppender(LOGGER_APPENDER_NAME);
        if (app != null) {
            logger.detachAppender(app);
            app.stop();
        }
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        context.getChronicle().addListener(getLogEntryHandler());
        if (context.getLogger() instanceof ch.qos.logback.classic.Logger) {
            addLoggerAppender((ch.qos.logback.classic.Logger) context.getLogger());
        }
    }

    @Override
    public void detach() {
        if (getLogger() instanceof ch.qos.logback.classic.Logger) {
            removeLoggerAppender((ch.qos.logback.classic.Logger) getLogger());
        }
        super.detach();
    }


    @Override
    public InputStream in() {
        if (in == null) {
            return System.in;
        } else {
            return in;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream out(Name stage, Name name) {
        return out();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream out() {
        if (out == null) {
            return System.out;
        } else {
            return out;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile(String path) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file;
        } else {
            return new File(getRootDirectory(), path);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getRootDirectory() {
        String rootDir = getContext().getString(ROOT_DIRECTORY_CONTEXT_KEY, System.getProperty("user.home"));
        File root = new File(rootDir);
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }


    @Override
    public InputStream in(String path) {
        try {
            return new FileInputStream(getFile(path));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
