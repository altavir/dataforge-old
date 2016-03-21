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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import hep.dataforge.names.Named;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.TargetNotProvidedException;
import hep.dataforge.io.IOManager;
import hep.dataforge.io.log.Log;
import hep.dataforge.io.log.LogEntry;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.navigation.AbstractProvider;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Окружение для выполнения действий (и не только). Имеет собственный лог и
 * набор свойств. Если при создании указывается предок контекста, то, что не
 * найденно в данном контексте рекурсивно ищется в предках. Является основой для
 * менеджеров.
 *
 * @author Alexander Nozik
 */
public class Context extends AbstractProvider implements ValueProvider, Logable, Named {

    private final Log rootLog;
    private final Context parent;
    private final String name;
    protected final Map<String, Value> properties = new ConcurrentHashMap<>();
    private final PluginManager pm;
    protected ProcessManager processManager = null;
    protected IOManager io = null;

    /**
     * Build context from metadata
     *
     * @param parent
     * @param name
     * @param config
     */
    public Context(Context parent, String name, Meta config) {
        this.pm = new PluginManager(this);
        this.parent = parent;
        this.rootLog = new Log(name, parent);
        this.name = name;
        if (config != null) {
            if (config.hasNode("property")) {
                for (Meta propertyNode : config.getNodes("property")) {
                    properties.put(propertyNode.getString("key"), propertyNode.getValue("value"));
                }
            }
        }
    }

    public Context(Context parent, String name) {
        this(parent, name, null);
    }

    public Context(String name) {
        this(GlobalContext.instance(), name);
    }

    /**
     * {@inheritDoc} namespace does not work
     */
    @Override
    public Value getValue(String path) {
        Value res = properties.get(path);
        if (res != null) {
            return res;
        } else if (parent != null) {
            return parent.getValue(path);
        } else {
            throw new NameNotFoundException(path);
        }
    }

    /**
     * {@inheritDoc} namespace does not work
     */
    @Override
    public boolean hasValue(String path) {
        return properties.containsKey(path) || (parent != null && parent.hasValue(path));
    }

    /**
     * Возвращает родительский контекст, если такового нет, то возвращает
     * глобальный контекст
     *
     * @return a {@link hep.dataforge.context.Context} object.
     */
    public Context getParent() {
        if (this.parent != null) {
            return parent;
        } else {
            return GlobalContext.instance();
        }
    }

    /**
     * Return IO manager of this context. By default parent IOManager is
     * returned.
     *
     * @return the io
     */
    public IOManager io() {
        if (io == null) {
            return parent.io();
        } else {
            return io;
        }
    }

    /**
     * Do not use this!!! use IOManager::attachTo instead
     * @param io 
     */
    public void attachIoManager(IOManager io) {
        io.setContext(this);

        LoggerContext loggerContext = getLogger().getLoggerContext();
        getLogger().detachAndStopAllAppenders();

        OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setContext(loggerContext);
        appender.setOutputStream(io.out());
        appender.start();

        getLogger().addAppender(appender);
        
        if (!io.out().equals(System.out)) {
            getLog().addLogListener((LogEntry t) -> {
                try {
                    io.out().write((t.toString() + "\n").getBytes());
                    io.out().flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }        
        this.io = io;
    }

    /**
     * Plugin manager for this Context
     *
     * @return
     */
    public final PluginManager pluginManager() {
        return this.pm;
    }
    
    public ProcessManager processManager(){
        if(this.processManager == null){
            if(getParent()!= null){
                return getParent().processManager();
            } else {
                return GlobalContext.instance().processManager();
            }
        } else {
            return processManager;
        }
    } 

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Check if object is provided in this namespace as is
     *
     * @param <T>
     * @param target a {@link java.lang.String} object.
     * @param name a {@link hep.dataforge.names.Name} object.
     * @return a boolean.
     */
    @SuppressWarnings("unchecked")
    public <T> T provideInNameSpace(String target, Name name) {
        if ("value".equals(target)) {
            return (T) getValue(name.toString());
        } else if (target.isEmpty() || "plugin".equals(target)) {
            return (T) pluginManager().getPlugin(name.toString());
        } else {
            throw new TargetNotProvidedException();
        }
    }

    /**
     * Provide an object from namespace as is (without namespace substitution)
     *
     * @param target
     * @param name
     * @return
     */
    public boolean providesInNameSpace(String target, Name name) {
        if ("value".equals(target)) {
            return hasValue(name.toString());
        } else if (target.isEmpty() || "plugin".equals(target)) {
            return pluginManager().hasPlugin(name.toString());
        } else {
            return false;
        }
    }

    /**
     * <p>
     * putValue.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link hep.dataforge.values.Value} object.
     */
    public void putValue(String name, Value value) {
        properties.put(name, value);
    }

    public void putValue(String name, Object value) {
        properties.put(name, Value.of(value));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Log getLog() {
        return this.rootLog;
    }

    @Override
    public boolean provides(String target, Name name) {
        return providesInNameSpace(target, name)
                || (name.nameSpace().equals(getName()) && providesInNameSpace(target, name.toNameSpace("")));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Object provide(String target, Name name) {
        if (providesInNameSpace(target, name)) {
            return provideInNameSpace(target, name);
        } else if (name.nameSpace().equals(getName())) {
            return provideInNameSpace(target, name.toNameSpace(""));
        } else {
            throw new NameNotFoundException(name.toString());
        }
    }

    public final void loadPlugin(Plugin plugin) {
        this.pluginManager().loadPlugin(plugin);
    }

    public final void loadPlugin(String tag) {
        this.pluginManager().loadPlugin(tag);
    }

    public Map<String, Value> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
