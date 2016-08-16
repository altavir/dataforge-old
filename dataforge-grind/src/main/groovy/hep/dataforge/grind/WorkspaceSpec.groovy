/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.GlobalContext
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.Template
import hep.dataforge.values.MapValueProvider
import hep.dataforge.workspace.BasicWorkspace
import hep.dataforge.workspace.Task
import hep.dataforge.workspace.Workspace

/**
 * A DSL helper to build workspace
 * @author Alexander Nozik
 */
@CompileStatic
class WorkspaceSpec extends Script {
    Workspace.Builder builder = BasicWorkspace.builder();

    /**
     * build context for the workspace using
     */
    def context(Closure cl) {
        def contextSpec = new ContextSpec();
        def code = cl.rehydrate(contextSpec, this, this);
        code.resolveStrategy = Closure.DELEGATE_ONLY;
        code();
        builder.setContext(contextSpec.build());
    }

    @Override
    Object run() {
        return builder;
    }

    private class ContextSpec {
        String name = "workspace";
        String parent = GlobalContext.instance().getName();
        Map properties = new HashMap();
        Map<String, Meta> pluginMap = new HashMap<>();

        Context build() {
            Context res = new Context(GlobalContext.getContext(parent), name)
            properties.each { key, value -> res.putValue(key.toString(), value) }
            pluginMap.forEach { String key, Meta meta -> res.pluginManager().loadPlugin(key.toString()).configure(meta) }
            return res;
        }

        def properties(Closure cl) {
            def spec = [:]//new PropertySetSpec();
            def code = cl.rehydrate(spec, this, this);
            code.resolveStrategy = Closure.DELEGATE_ONLY;
            code();
            properties.putAll(spec);
        }

        def plugin(String key) {
            pluginMap.put(key, Meta.empty())
        }

        def plugin(String key, Closure cl) {
            pluginMap.put(key, GrindUtils.buildMeta(cl))
        }

        def rootDir(String path) {
            properties.put("rootDir", path);
        }
    }

    /**
     * Set workspace data
     * @param cl
     * @return
     */
    def data(Closure cl) {
        def spec = new DataSpec();
        def code = cl.rehydrate(spec, this, this);
        code.resolveStrategy = Closure.DELEGATE_ONLY;
        code();
    }

    private class DataSpec {
        def file(String name, String path, @DelegatesTo(GrindMetaBuilder) Closure fileMeta) {
            WorkspaceSpec.this.builder.putFile(name, path, GrindUtils.buildMeta(fileMeta));
        }
        //TODO extends data specification
    }

    /**
     * Define new task using task builder
     * @param taskName
     * @param cl
     * @return
     */
    def task(String taskName, @DelegatesTo(TaskSpec) Closure cl) {
        def taskSpec = new TaskSpec(taskName);
        def code = cl.rehydrate(taskSpec, this, this);
        code.resolveStrategy = Closure.DELEGATE_ONLY;
        code();
        builder.loadTask(taskSpec.build());
    }

    /**
     * load existing task
     * @param task
     * @return
     */
    def loadTask(Task task) {
        builder.loadTask(task);
    }

    /**
     * Load existing task by class
     * @param taskClass
     * @return
     */
    def loadTask(Class<? extends Task> taskClass) {
        builder.loadTask(taskClass.newInstance());
    }

    /**
     * Load meta using grind meta builder
     * @param closure
     * @return
     */
    def meta(Closure closure) {
        MetaSpec spec = new MetaSpec();
        def code = closure.rehydrate(spec, this, this);
        code.resolveStrategy = Closure.DELEGATE_FIRST;
        code();
    }

    private class MetaSpec {
        def methodMissing(String methodName, Closure par) {
            WorkspaceSpec.this.builder.loadMeta(GrindUtils.buildMeta(methodName, par));
        }
    }

    def meta(String name, Closure closure) {
        this.builder.loadMeta(GrindUtils.buildMeta(name, closure));
    }

    def meta(Meta meta) {
        this.builder.loadMeta(meta);
    }

    def meta(String name, Meta template, Map map) {
        this.builder.loadMeta(compileMeta(template, map).rename(name));
    }

    /**
     * Build meta(Builder) using builder but do not add it to workspace
     * @param name
     * @param closure
     * @return
     */
    MetaBuilder buildMeta(String name, Closure closure) {
        return GrindUtils.buildMeta(name, closure);
    }

    /**
     * Complile meta using template and given meta as a data source
     * @param template
     * @param data
     * @return
     */
    MetaBuilder compileMeta(Meta template, Meta data) {
        return Template.compileTemplate(template, data);
    }

    MetaBuilder compileMeta(Meta template, Closure cl) {
        return compileMeta(template, buildMeta("", cl));
    }

    MetaBuilder compileMeta(Meta template, Map map) {
        return Template.compileTemplate(template, map);
    }

    /**
     * Use map as a value provider and given meta as meta provider
     * @param template
     * @param map
     * @param cl
     * @return
     */
    MetaBuilder compileMeta(Meta template, Map map, Closure cl) {
        Template tmp = new Template(template);
        return tmp.compile(new MapValueProvider(map), buildMeta("", cl));
    }

}

