package hep.dataforge.grind

import hep.dataforge.actions.OneToOneAction
import hep.dataforge.context.Context
import hep.dataforge.io.reports.Reportable
import hep.dataforge.meta.Laminate

/**
 * Created by darksnake on 07-Aug-16.
 */
class GrindOneToOneAction extends OneToOneAction {
    public static GrindOneToOneAction build(Closure closure){
        def res = new GrindOneToOneAction()
        res.closure = closure;
        return res;
    }

    Closure closure;
    Map params = ["name":"@dynamic"];

    @Override
    Context getContext() {
        params.getOrDefault("context", super.getContext())
    }

    @Override
    String getName() {
        params.get("name")
    }

    @Override
    Class getInputType() {
        params.getOrDefault("inputType", super.getInputType())
    }

    @Override
    Class getOutputType() {
        params.getOrDefault("outputTupe", super.getOutputType())
    }

    @Override
    protected Object execute(Reportable log, String name, Laminate meta, Object input) {
        if (closure != null) {
            return closure.call(log, name, meta, input);
        } else {
            return input;
        }

    }
}
