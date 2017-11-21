package hep.dataforge.workspace.templates;

import hep.dataforge.actions.Action;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;
import hep.dataforge.workspace.tasks.MultiStageTask;
import hep.dataforge.workspace.tasks.Task;
import hep.dataforge.workspace.tasks.TaskModel;

import java.util.List;
import java.util.stream.Collectors;

import static hep.dataforge.actions.ActionUtils.*;

public class ActionTaskTemplate implements TaskTemplate {
    @Override
    public String getName() {
        return "actions";
    }

    @Override
    public Task build(Context context, Meta meta) {
        List<Action> actions = meta.getMetaList(ACTION_NODE_KEY).stream()
                .map(actionMeta -> buildAction(context, actionMeta.getString(ACTION_TYPE, SEQUENCE_ACTION_TYPE)))
                .collect(Collectors.toList());

        return new ActionTask(meta.getString("name"), actions);
    }

    private static class ActionTask extends MultiStageTask<Object>{

        private final String name;
        private final List<Action> actions;

        private ActionTask(String name, List<Action> actions) {
            super(Object.class);
            this.name = name;
            this.actions = actions;
        }

        @Override
        protected MultiStageTaskState transform(TaskModel model, MultiStageTaskState state) {
            DataNode res = state.getData();
            for (Action action : actions) {
                Meta actionMeta = model.getMeta().getMetaOrEmpty(name);
                res = action.run(model.getContext(), res, actionMeta);
                state.setData(action.getName(), res);
            }
            state.finish(res);
            return state;
        }

        @Override
        protected void buildModel(TaskModel.Builder model, Meta meta) {

        }

        @Override
        public String getName() {
            return name;
        }
    }
}
