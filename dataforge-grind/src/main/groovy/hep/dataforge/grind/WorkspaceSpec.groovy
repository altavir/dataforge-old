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
import hep.dataforge.workspace.BasicWorkspace
import hep.dataforge.workspace.Task
import hep.dataforge.workspace.Workspace

/**
 * A DSL helper to build workspace
 * @author Alexander Nozik
 */
@CompileStatic
class WorkspaceSpec {
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
            builder.putFile(name, path, GrindUtils.buildMeta(fileMeta));
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
    def loadTask(Class<Task> taskClass) {
        builder.loadTask(taskClass.newInstance());
    }

    /**
     * Load meta using grind meta builder
     * @param closure
     * @return
     */
    def meta(Closure closure) {
        this.builder.loadMeta(GrindUtils.buildMeta(closure));
    }

    def meta(String name, Closure closure) {
        this.builder.loadMeta(GrindUtils.buildMeta(name, closure));
    }

    def meta(Meta meta) {
        this.builder.loadMeta(meta);
    }

    MetaBuilder buildMeta(String name, Closure closure) {
        return GrindUtils.buildMeta(name, closure);
    }

    Meta compileMeta(Meta template, Meta data) {
        return Template.compileTemplate(template, data);
    }

    Meta compileMeta(Meta template, Closure cl) {
        return compileMeta(template, buildMeta("", cl));
    }

//    private class MetaSpec {
//        String name
//
//        def methodMissing(String methodName) {
//            this.name = methodName
//        }
//
//        def call(Closure closure) {
//            if (name) {
//                builder.loadMeta(name, GrindUtils.buildMeta(closure));
//            } else {
//                throw new RuntimeException("Meta name not provided");
//            }
//        }
//    }

}

