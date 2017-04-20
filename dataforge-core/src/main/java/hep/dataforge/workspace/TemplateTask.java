/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.actions.Action;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Template;

import java.util.function.UnaryOperator;

import static hep.dataforge.actions.ActionUtils.*;

/**
 * A task consisting of sequence of actions. Action sequence is built using {@link hep.dataforge.actions.ActionUtils} {@code buildAction} method.
 * If template is provided, then task meta is used to fill that template and result is passed to builder.
 *
 * @author Alexander Nozik
 */
public abstract class TemplateTask<T> extends MultiStageTask<T> {

    private final String name;
    private final UnaryOperator<Meta> template;

    public TemplateTask(String name, Class<T> type, Template template) {
        super(type);
        this.name = name;
        this.template = template;
    }

    public TemplateTask(String name, Class<T> type, Meta template) {
        super(type);
        this.name = name;
        this.template = new Template(template);
    }

    public TemplateTask(String name, Class<T> type) {
        super(type);
        this.name = name;
        this.template = UnaryOperator.identity();
    }

    @Override
    protected void transform(TaskModel model, MultiStageTaskState state) {
        DataNode res = state.getData();
        Meta config = template.apply(model.meta());
        for (Meta actionMeta : config.getMetaList(ACTION_NODE_KEY)) {
            String actionType = actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
            Action action = buildAction(model.getContext(), actionType);
            res = action.run(model.getContext(), res, actionMeta);
            state.setData(actionType, res);
        }
        state.finish(res);
    }

    @Override
    public String getName() {
        return name;
    }

}
