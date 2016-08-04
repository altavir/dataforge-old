/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind

import hep.dataforge.context.Context
import hep.dataforge.context.GlobalContext
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.BasicWorkspace
import hep.dataforge.workspace.Task
import hep.dataforge.workspace.TaskBuilder
import hep.dataforge.workspace.Workspace

/**
 *
 * @author Alexander Nozik
 */
class WorkspaceBuilder {
    Workspace.Builder builder = BasicWorkspace.builder();

    /**
     * set context for the workspace
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
        String parent = GlobalContext.instance();
        Map properties;

        Context build() {
            Context res = new Context(GlobalContext.getContext(parent), name)
            properties.each { key, value -> res.putValue(key, value) }
            return res;
        }

        def properties(Closure cl) {
            def spec = new PropertySetSpec();
            def code = cl.rehydrate(spec, this, this);
            code.resolveStrategy = Closure.DELEGATE_ONLY;
            code();
            properties.putAll(spec.build());
        }

        //TODO add plugin spec
    }

    /**
     * Specification to add property sets
     */
    private class PropertySetSpec {
        private def storage = [:]

        def propertyMissing(String name, value) { storage[name] = value }

        def propertyMissing(String name) { storage[name] }

        def build() {
            return storage;
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
        def file(String name, String path, Closure<Meta> fileMeta) {
            def metaSpec = new GrindMetaBuilder()
            def metaExec = fileMeta.rehydrate(metaSpec, this, this);
            metaExec.resolveStrategy = Closure.DELEGATE_ONLY;
            builder.putFile(name, path, metaExec())
        }
        //TODO extends data specification
    }

    /**
     * Build new task using task builder
     * @param taskName
     * @param cl
     * @return
     */
    def task(String taskName, Closure cl) {
        def taskSpec = new TaskSpec();
        def code = cl.rehydrate(taskSpec, this, this);
        code.resolveStrategy = Closure.DELEGATE_ONLY;
        code();
        builder.loadTask(taskSpec.build());
    }

    def wrapTask

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

    private class TaskSpec extends TaskBuilder {

    }
}

