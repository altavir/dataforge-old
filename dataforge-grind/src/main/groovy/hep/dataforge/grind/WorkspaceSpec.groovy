/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.context.Plugin
import hep.dataforge.data.Data
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.workspace.BasicWorkspace
import hep.dataforge.workspace.Task
import hep.dataforge.workspace.Workspace

/**
 * A DSL helper to build workspace
 * @author Alexander Nozik
 */
@CompileStatic
class WorkspaceSpec {
    private Workspace.Builder builder;
    private final Context context;

    WorkspaceSpec(Context context) {
        this.builder = BasicWorkspace.builder();
        this.builder.setContext(context);
        this.context = context
    }

    /**
     * build context for the workspace using closure
     */
    def context(Closure cl) {
        def contextSpec = new ContextSpec()
        def code = cl.rehydrate(contextSpec, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        builder.setContext(contextSpec.build().withParent(context))
    }

    Workspace.Builder build() {
        return builder
    }

    private class ContextSpec {
        String name = "workspace"
        Map properties = new HashMap()
        Map<String, Meta> pluginMap = new HashMap<>()

        Context build() {
            //using current context as a parent for workspace context
            Context res = Global.getContext(name).withParent(context)
            properties.each { key, value -> res.putValue(key.toString(), value) }
            pluginMap.forEach { String key, Meta meta ->
                Plugin plugin
                if (res.pluginManager().hasPlugin(key)) {
                    plugin = res.pluginManager().getPlugin(key)
                } else {
                    plugin = res.pluginManager().load(key.toString())
                }
                if(plugin instanceof Configurable) {
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
    def data(Closure cl) {
        def spec = new DataSpec()
        def code = cl.rehydrate(spec, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
    }

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

        def load(Object... args) {
            loadFromMeta(Grind.buildMeta(args))
        }

        def loadFromMeta(Meta meta) {
            //TODO remove control values from meta
            WorkspaceSpec.this.builder.loadData(
                    meta.getString("as", ""),
                    meta.getString("loader"),
                    meta
            )
        }

        //TODO extend data specification
    }

    /**
     * Define new task using task builder
     * @param taskName
     * @param cl
     * @return
     */
    def task(String taskName, @DelegatesTo(TaskSpec) Closure cl) {
        def taskSpec = new TaskSpec(taskName)
        def code = cl.rehydrate(taskSpec, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        builder.loadTask(taskSpec.build())
    }

    /**
     * load existing task
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
     * Load meta using grind meta builder
     * @param closure
     * @return
     */
    def configuration(Closure closure) {
        MetaSpec spec = new MetaSpec()
        def code = closure.rehydrate(spec, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
    }

    private class MetaSpec {
        def methodMissing(String methodName, Closure par) {
            WorkspaceSpec.this.builder.loadMeta(Grind.buildMeta(methodName, par))
        }
    }

    def configuration(String name, Closure closure) {
        this.builder.loadMeta(Grind.buildMeta(name, closure))
    }

    def configuration(Meta meta) {
        this.builder.loadMeta(meta)
    }

//    def meta(String name, Meta template, Map map) {
//        this.builder.loadMeta(template.compile(map).rename(name));
//    }

    /**
     * Build meta(Builder) using builder but do not add it to workspace
     * @param name
     * @param closure
     * @return
     */
    MetaBuilder buildMeta(String name, Closure closure) {
        return Grind.buildMeta(name, closure)
    }

    MetaBuilder buildMeta(String name, Map<String, Object> values, Closure closure) {
        MetaBuilder res = Grind.buildMeta(name, closure)
        values.forEach { key, value -> res.setValue(key.toString(), value) }
        return res
    }

}

