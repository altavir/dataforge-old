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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.BasicIOManager;
import hep.dataforge.io.IOManager;
import hep.dataforge.io.reports.LogEntry;
import hep.dataforge.utils.ReferenceRegistry;
import hep.dataforge.values.Value;
import org.slf4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A singleton global context. Automatic root for the whole context hierarchy. Also stores the registry for active contexts.
 *
 * @author Alexander Nozik
 */
public class Global extends Context {

    private static final ReferenceRegistry<Context> contextRegistry = new ReferenceRegistry();
    private static final ExecutorService dispatchThreadExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread res = new Thread(r, "DF_DISPATCH");
//        res.setDaemon(false);
        res.setPriority(Thread.MAX_PRIORITY);
        return res;
    });
    private static Global instance = new Global();


    private Global() {
        super("GLOBAL");
        Locale.setDefault(Locale.US);
    }

    /**
     * A single thread executor for DataForge messages dispatch. No heavy calculations should be done on this thread
     *
     * @return
     */
    public static ExecutorService dispatchThreadExecutor() {
        return dispatchThreadExecutor;
    }

    public static Global instance() {
        return instance;
    }

    public static Logger logger() {
        return instance.logger;
    }

    /**
     * Get previously build context o build a new one
     *
     * @param name
     * @return
     */
    public static synchronized Context getContext(String name) {
        return contextRegistry.findFirst(ctx -> ctx.getName().equals(name)).orElseGet(() -> {
            Context ctx = new Context(name);
            contextRegistry.add(ctx);
            return ctx;
        });
    }

    public static File getFile(String path) {
        return instance.io().getFile(path);
    }

    public static PrintWriter out() {
        return new PrintWriter(instance.io().out());
    }

    /**
     * Close all contexts and terminate framework
     */
    public static void terminate() {
        org.slf4j.Logger logger = instance().getLogger();
        try {
            instance().close();
        } catch (Exception e) {
            logger.error("Exception while terminating DataForge framework");
        }
    }

    @Override
    public IOManager io() {
        if (this.io == null) {
            setIO(new BasicIOManager());
            getLog().addListener((LogEntry t) -> {
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
        getLogger().info("Shutting down GLOBAL");
        for (Context ctx : contextRegistry) {
            ctx.close();
        }
        dispatchThreadExecutor.shutdown();
        super.close();
    }

}
