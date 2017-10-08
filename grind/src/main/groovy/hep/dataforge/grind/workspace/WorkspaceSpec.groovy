/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hep.dataforge.grind.workspace

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.grind.Grind
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.BasicWorkspace
import hep.dataforge.workspace.Workspace
import hep.dataforge.workspace.tasks.Task

/**
 * A DSL helper to build workspace
 * @author Alexander Nozik
 */
@CompileStatic
class WorkspaceSpec {
    private Workspace.Builder builder;
//    private final Context context;

    /**
     * Create a new specification for a workspace
     * @param context - the context for specification it is by default used as a parent for resulting workspace
     */
    WorkspaceSpec(Context context) {
        this.builder = BasicWorkspace.builder();
        this.builder.setContext(context);
//        this.context = context
    }

    /**
     * builder context for the workspace using closure
     */
    def context(@DelegatesTo(value = ContextSpec, strategy = Closure.DELEGATE_FIRST) Closure cl) {
        def contextSpec = new ContextSpec(builder.context)
        def code = cl.rehydrate(contextSpec, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        builder.setContext(contextSpec.build())
    }

    Workspace.Builder getBuilder() {
        return builder
    }

    /**
     * Set workspace data
     * @param cl
     * @return
     */
    void data(@DelegatesTo(value = DataNodeSpec, strategy = Closure.DELEGATE_FIRST) Closure cl) {
        builder.loadData("", DataNodeSpec.buildNode(builder.context, cl))
    }

    /**
     * Load a task into the workspace. One can use task libraries like {@link DefaultTaskLib} to define task builders
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
    def targets(Closure closure) {
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

    def target(Map parameters = [:], String name, Closure closure = null) {
        this.builder.target(Grind.buildMeta(name, parameters, closure))
    }

    def target(Meta meta) {
        this.builder.target(meta)
    }
}

