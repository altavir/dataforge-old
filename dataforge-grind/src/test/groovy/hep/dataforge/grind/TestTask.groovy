package hep.dataforge.grind

import hep.dataforge.data.DataSet
import hep.dataforge.meta.MetaUtils
import hep.dataforge.workspace.MultiStageTask
import hep.dataforge.workspace.TaskModel

/**
 * Created by darksnake on 04-Aug-16.
 */
class TestTask extends MultiStageTask {
    TestTask() {
        super(Object)
    }

    @Override
    String getName() {
        return "testTask"
    }

    @Override
    protected MultiStageTaskState transform(TaskModel model, MultiStageTaskState state) {
        DataSet.Builder b = DataSet.builder()
        model.context.getProperties().forEach { key, value ->
            b.putStatic(key, value);
        }
        MetaUtils.valueStream(model.meta()).forEach { pair ->
            b.putStatic("meta." + pair.getKey(), pair.getValue())
        }

        state.finish(b.build())
    }

    @Override
    protected TaskModel transformModel(TaskModel model) {
        return model;
    }
}
