package hep.dataforge.grind

import hep.dataforge.computation.ProgressCallback
import hep.dataforge.context.Context
import hep.dataforge.data.DataSet
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaUtils
import hep.dataforge.workspace.MultiStageTask
import hep.dataforge.workspace.MultiStageTaskState

/**
 * Created by darksnake on 04-Aug-16.
 */
class TestTask extends MultiStageTask {
    @Override
    String getName() {
        return "testTask"
    }

    @Override
    protected void transform(ProgressCallback callback, Context context, MultiStageTaskState state, Meta config) {
        DataSet.Builder b = DataSet.builder()
        context.getProperties().forEach { key, value ->
            b.putStatic(key, value);
        }
        MetaUtils.valueStream(config).forEach { pair -> b.putStatic("meta." + pair.getKey(), pair.getValue()) }

        state.finish(b.build())
    }
}
