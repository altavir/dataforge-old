package hep.dataforge.grind

import hep.dataforge.computation.WorkManager
import hep.dataforge.context.Context
import hep.dataforge.data.DataSet
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.GenericTask
import hep.dataforge.workspace.TaskState

/**
 * Created by darksnake on 04-Aug-16.
 */
class TestTask extends GenericTask {
    @Override
    String getName() {
        return "testTask"
    }

    @Override
    protected TaskState transform(WorkManager.Callback callback, Context context, TaskState state, Meta config) {
        DataSet.Builder b = DataSet.builder()
        context.getProperties().forEach { key, value ->
            b.putStatic(key, value);
        }

        state.finish(b.build())
    }
}
