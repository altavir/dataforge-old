/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Template;

/**
 * A task using meta values and node substitution to shorten description
 *
 * @author Alexander Nozik
 * @param <T>
 */
public abstract class TemplateTask<T> extends GenericTask<T> {

    @Override
    protected Meta getTaskMeta(Context context, TaskModel model) {
        Template template = getTemplate();
        if (template == null) {
            context.getLogger().debug("Template in task {} not found.", getName());
            return model.meta();
        } else {
            return getTemplate().compile(model.meta());
        }
    }

    protected abstract Template getTemplate();
}
