/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;

/**
 * A task using meta values and node substitution to shorten description
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class ParametricTask<T> implements Task<T> {

    private final Task<T> task;
    private final Meta template;

    public ParametricTask(Task<T> task, Meta template) {
        this.task = task;
        this.template = template;
    }

    @Override
    public String getName() {
        return task.getName();
    }

    @Override
    public DataNode<T> run(Workspace workspace, Meta config) {
        return task.run(workspace, MetaUtils.compileTemplate(template, config, config));
    }

}
