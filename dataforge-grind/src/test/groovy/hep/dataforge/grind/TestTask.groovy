package hep.dataforge.grind

import hep.dataforge.computation.WorkManager
import hep.dataforge.context.Context
import hep.dataforge.data.DataSet
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaUtils
import hep.dataforge.values.Value
import hep.dataforge.workspace.GenericTask
import hep.dataforge.workspace.TaskState
import javafx.util.Pair

import java.util.function.Consumer

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
        MetaUtils.valueStream(config).forEach { pair -> b.putStatic("meta." + pair.getKey(), pair.getValue()) }

        state.finish(b.build())
    }
}
