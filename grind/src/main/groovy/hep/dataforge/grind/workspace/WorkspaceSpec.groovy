/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind.workspace

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Plugin
import hep.dataforge.data.Data
import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.grind.Grind
import hep.dataforge.grind.GrindMetaBuilder
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Meta
import hep.dataforge.names.Named
import hep.dataforge.workspace.BasicWorkspace
import hep.dataforge.workspace.Workspace
import hep.dataforge.workspace.tasks.Task
import hep.dataforge.workspace.templates.TaskFactory

import java.util.function.Supplier
import java.util.stream.StreamSupport

/**
 * A DSL helper to builder workspace
 * @author Alexander Nozik
 */
@CompileStatic
class WorkspaceSpec {
    private Workspace.Builder builder;
    private final Context context;

    /**
     * Create a new specification for a workspace
     * @param context - the context for specification it is by default used as a parent for resulting workspace
     */
    WorkspaceSpec(Context context) {
        this.builder = BasicWorkspace.builder();
        this.builder.setContext(context);
        this.context = context
    }

    /**
     * builder context for the workspace using closure
     */
    def context(Closure cl) {
        def contextSpec = new ContextSpec()
        def code = cl.rehydrate(contextSpec, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        builder.setContext(contextSpec.build())
    }

    Workspace.Builder getBuilder() {
        return builder
    }

    /**
     * A specification to builder context via grind workspace definition
     */
    private class ContextSpec {
        String name = "workspace"
        Map properties = new HashMap()
        Map<String, Meta> pluginMap = new HashMap<>()

        Context build() {
            //using current context as a parent for workspace context
            Context res = Context.builder(name, context).build()
            properties.each { key, value -> res.putValue(key.toString(), value) }
            pluginMap.forEach { String key, Meta meta ->
                Plugin plugin = res.pluginManager().getOrLoad(key)
                if (plugin instanceof Configurable) {
                    (plugin as Configurable).configure(meta)
                }

            }
            return res
        }

        def properties(Closure cl) {
            def spec = [:]//new PropertySetSpec();
            def code = cl.rehydrate(spec, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()
            properties.putAll(spec)
        }

        def plugin(String key) {
            pluginMap.put(key, Meta.empty())
        }

        def plugin(String key, Closure cl) {
            pluginMap.put(key, Grind.buildMeta(cl))
        }

        def rootDir(String path) {
            properties.put("rootDir", path)
        }
    }

    /**
     * Set workspace data
     * @param cl
     * @return
     */
    def data(@DelegatesTo(DataSpec) Closure cl) {
        def spec = new DataSpec()
        def code = cl.rehydrate(spec, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
    }

    /**
     * A specification to builder workspace data
     */
    private class DataSpec {
        def files(String place, String path, @DelegatesTo(GrindMetaBuilder) Closure fileMeta) {
            WorkspaceSpec.this.builder.loadFileData(place, path, Grind.buildMeta(fileMeta))
        }

        def files(String place, String path) {
            WorkspaceSpec.this.builder.loadFileData(place, path)
        }

        /**
         * Put a static resource as data
         * @param place
         * @param path
         * @return
         */
        def resource(String place, String path) {
            URI uri = URI.create(path)
            WorkspaceSpec.this.builder.loadData(place, Data.buildStatic(uri))
        }

        def load(Map values = [:], String nodeName = "",
                 @DelegatesTo(GrindMetaBuilder) Closure cl = null) {
            loadFromMeta(Grind.buildMeta(values, nodeName, cl))
        }

        def loadFromMeta(Meta meta) {
            //TODO remove control values from meta
            WorkspaceSpec.this.builder.loadData(
                    meta.getString("as", ""),
                    meta.getString("loader"),
                    meta
            )
        }

        /**
         * Add a dynamic data
         * @param name
         * @param meta
         * @param type
         * @param cl
         */
        def <R> void item(String name, Meta meta = Meta.empty(), Class<R> type = Object, Supplier<R> cl) {
            WorkspaceSpec.this.builder.loadData(name, Data.generate(type, meta, cl))
        }

        /**
         * Create a static data item in the workspace
         * @param name
         * @param object
         * @return
         */
        def item(String name, Object object) {
            WorkspaceSpec.this.builder.loadData(name, Data.buildStatic(object));
        }

        /**
         * Load static data from map
         * @param items
         * @return
         */
        def items(Map<String, ?> items) {
            items.each { key, value ->
                item(key, Data.buildStatic(value))
            }
        }

        /**
         *        Load static data from collection of Named objects
         */
        def items(Collection<? extends Named> something) {
            something.each {
                item(it.name, Data.buildStatic(it))
            }
        }

        def item(Named object) {
            item(object.name, Data.buildStatic(object));
        }

        //TODO extend data specification
    }

    /**
     * Create a task but do not load it. use {@code task template{...}} to load
     * @param parameters
     * @param taskName
     * @param cl
     * @return
     */
    Task template(Map parameters = Collections.emptyMap(), String taskName, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        Meta meta = Grind.buildMeta(parameters, taskName, cl);
        return StreamSupport.stream(ServiceLoader.load(TaskFactory).spliterator(), false)
                .filter { it.name == meta.getName() }
                .map { it.build(context, meta) }
                .findFirst().orElseThrow { new NameNotFoundException("Task template with name $taskName not found") }
    }

    /**
     * Define new task using task factory
     * @param taskName
     * @param cl
     * @return
     */
    Task build(String taskName, @DelegatesTo(value = TaskSpec, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        def taskSpec = new TaskSpec().name(taskName);
        def code = closure.rehydrate(taskSpec, null, null)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code.call()
        return taskSpec.build();
    }

    /**
     * register existing task
     * @param task
     * @return
     */
    def task(Task task) {
        builder.loadTask(task)
    }

    /**
     * Load existing task by class
     * @param taskClass
     * @return
     */
    def task(Class<? extends Task> taskClass) {
        builder.loadTask(taskClass.newInstance())
    }

    /**
     * Load meta target using grind meta builder
     * @param closure
     * @return
     */
    def target(Closure closure) {
        MetaSpec spec = new MetaSpec()
        def code = closure.rehydrate(spec, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
    }

    private class MetaSpec {
        def methodMissing(String methodName, Closure cl) {
            WorkspaceSpec.this.builder.target(Grind.buildMeta(methodName, cl))
        }
    }

    def target(String name, Closure closure) {
        this.builder.target(Grind.buildMeta(name, closure))
    }

    def target(Meta meta) {
        this.builder.target(meta)
    }
}

