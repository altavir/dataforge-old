/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.actions.Action;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.goals.ProgressCallback;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Template;

import java.util.function.UnaryOperator;

import static hep.dataforge.actions.ActionUtils.*;

/**
 * A task consisting of sequence of actions. Using template if it is provided.
 * The sequence is defined on flight using provided meta.
 *
 * @author Alexander Nozik
 */
public abstract class TemplateTask extends MultiStageTask {

    private final String name;
    private final UnaryOperator<Meta> template;

    public TemplateTask(String name, Template template) {
        this.name = name;
        this.template = template;
    }

    public TemplateTask(String name, Meta template) {
        this.name = name;
        this.template = new Template(template);
    }

    public TemplateTask(String name) {
        this.name = name;
        this.template = UnaryOperator.identity();
    }

    @Override
    protected void transform(ProgressCallback callback, Context context, MultiStageTaskState state, Meta config) {
        DataNode res = state.getData();
        config = template.apply(config);
        for (Meta actionMeta : config.getMetaList(ACTION_NODE_KEY)) {
            String actionType = actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
            Action action = buildAction(context, actionType);
            res = action.run(context, res, actionMeta);
            state.setData(actionType, res);
        }
        state.finish(res);
    }

    @Override
    public String getName() {
        return name;
    }

}
