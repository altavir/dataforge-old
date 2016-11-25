package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.actions.Action
import hep.dataforge.data.DataNode
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Template
import hep.dataforge.workspace.GatherTask
import hep.dataforge.workspace.Task
import hep.dataforge.workspace.TaskModel
import hep.dataforge.workspace.Workspace
import javafx.util.Pair

import java.util.function.UnaryOperator

/**
 * A specification for Grind dynamic task builder. Dynamic task will always work slower than statically compiled one
 * Created by darksnake on 07-Aug-16.
 */
@CompileStatic
class TaskSpec {
    private final String name;
    private Task prototype = new GatherTask();
    private UnaryOperator<Meta> trans = UnaryOperator.identity();
    private List<Pair<Action, UnaryOperator<Meta>>> actions = new ArrayList<>();

    TaskSpec(String name) {
        this.name = name
    }

    def prototype(Task proto, Closure template) {
        this.prototype = proto;
        trans = new Template(Grind.buildMeta { template });
    }

    def prototype(Task proto) {
        this.prototype = proto;
    }

    def prototype(Class<Task> proto, Closure template) {
        prototype(proto.newInstance(), template);
    }

    def prototype(Class<Task> proto) {
        prototype(proto.newInstance());
    }

//    def action(Map params, Closure action){
//        switch (params.getOrDefault("type","pipe")){
//            case "pipe":
//
//        }
//    }

    DynamicTask build() {
        return new DynamicTask();
    }

    private class DynamicTask implements Task {

        @Override
        TaskModel build(Workspace workspace, Meta taskConfig) {
            return prototype.build(workspace, trans.apply(taskConfig));
        }

        @Override
        void validate(TaskModel model) {
            prototype.validate(model);
        }

        @Override
        DataNode run(TaskModel model) {
            DataNode<?> res = prototype.run(model);
            for (Pair<Action, UnaryOperator<Meta>> pair in actions) {
                res = pair.key.run(res, pair.value.apply(model.meta()));
            }
            return res;
        }

        @Override
        String getName() {
            return name
        }
    }


}
