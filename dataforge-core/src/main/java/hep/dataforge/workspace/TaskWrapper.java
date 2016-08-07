/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Template;


/**
 * A task that wraps other task, but uses a template to transform meta
 * @param <R>
 */
public class TaskWrapper<R> implements Task<R> {
    
    private final String name;
    private final Task<R> task;
    private final Meta template;

    public TaskWrapper(String name, Task<R> task, Meta template) {
        this.name = name;
        this.task = task;
        this.template = template;
    }
    
    @Override
    public TaskModel build(Workspace workspace, Meta taskConfig) {
        return task.build(workspace, Template.compileTemplate(template, taskConfig));
    }

    @Override
    public void validate(TaskModel model) {
        task.validate(model);
    }

    @Override
    public DataNode<R> run(TaskModel model) {
        return task.run(model);
    }

    @Override
    public String getName() {
        return name;
    }
    
}
