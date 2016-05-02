/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import static hep.dataforge.actions.ActionUtils.ACTION_NODE_KEY;
import static hep.dataforge.actions.ActionUtils.ACTION_TYPE;
import static hep.dataforge.actions.ActionUtils.SEQUENCE_ACTION_TYPE;
import static hep.dataforge.actions.ActionUtils.buildAction;
import hep.dataforge.context.Context;
import hep.dataforge.context.ProcessManager;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;

/**
 * A task consisting of sequence of actions
 *
 * @author Alexander Nozik
 */
public class ActionTask extends GenericTask {

    private final String name;
//    private Meta actionMeta;

    public ActionTask(String name) {
        this.name = name;
    }

    @Override
    protected TaskState transform(ProcessManager.Callback callback, Context context, TaskState state, Meta config) {
        //TODO add fixed action meta and overrides
        DataNode res = state.getData();
        for (Meta action : config.getNodes(ACTION_NODE_KEY)) {
            String actionType = action.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE);
            res = buildAction(context, actionType).withParentProcess(callback.processName()).run(context, res, action);
            state.setData(actionType, res);
        }
        state.finish(res);
        return state;
    }

    @Override
    public String getName() {
        return name;
    }

}
