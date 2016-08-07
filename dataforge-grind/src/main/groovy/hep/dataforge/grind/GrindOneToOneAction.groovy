package hep.dataforge.grind

import hep.dataforge.actions.OneToOneAction
import hep.dataforge.context.Context
import hep.dataforge.io.reports.Reportable
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta

/**
 * Created by darksnake on 07-Aug-16.   
 */
class GrindOneToOneAction extends OneToOneAction {
    public static GrindOneToOneAction build(Closure triFunction) {
        def res = new GrindOneToOneAction(triFunction)
        return res;
    }

//    public static GrindOneToOneAction build(BiFunction<Meta, Object, Object> biFunction) {
//        def res = new GrindOneToOneAction()
//        res.action = { String name, Meta meta, Object input -> biFunction.apply(meta, input) };
//        return res;
//    }
//
//    public static GrindOneToOneAction build(Function<Object, Object> function) {
//        def res = new GrindOneToOneAction()
//        res.action = { String name, Meta meta, Object input -> function.apply(input) };
//        return res;
//    }

    Closure action;
    Map params = ["name": "@dynamic"];

    GrindOneToOneAction(Closure action) {
        this.action = action
    }

    GrindOneToOneAction(Map params, Closure action) {
        this.action = action
        this.params.putAll(params)
    }

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
        if (action != null) {
            return new OneToOneCallable(log,name,meta,input).execute(action);
        } else {
            return input;
        }

    }

    private static class OneToOneCallable {
        Reportable log
        String name
        Laminate meta
        Object input

        OneToOneCallable(Reportable log, String name, Laminate meta, Object input) {
            this.log = log
            this.name = name
            this.meta = meta
            this.input = input
        }

        Object execute(Closure closure){
            Closure rehydrated = closure.rehydrate(this,null,null);
            rehydrated.setResolveStrategy(Closure.DELEGATE_ONLY);
            return rehydrated.run();
        }
    }
}
