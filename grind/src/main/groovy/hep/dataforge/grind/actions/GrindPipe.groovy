package hep.dataforge.grind.actions

import groovy.transform.CompileStatic
import hep.dataforge.actions.OneToOneAction
import hep.dataforge.context.Context
import hep.dataforge.io.history.History
import hep.dataforge.meta.Laminate

/**
 * Created by darksnake on 07-Aug-16.   
 */
@CompileStatic
class GrindPipe<T, R> extends OneToOneAction<T, R> {
    static <T,R> GrindPipe<T,R> build(Map params = [:], @DelegatesTo(OneToOneCallable) Closure<R> triFunction) {
        def res = new GrindPipe(params, triFunction)
        return res;
    }

    Closure action;
    Map params = ["name": "@dynamic"];

    GrindPipe(Map params = [:], @DelegatesTo(OneToOneCallable) Closure action) {
        this.action = action
        this.params.putAll(params)
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
    protected Object execute(Context context, String name, T input, Laminate meta) {
        if (action != null) {
            return new OneToOneCallable<T, R>(context.getChronicle(name), name, meta, input).execute(action);
        } else {
            return input;
        }

    }

    static class OneToOneCallable<T, R> {
        History log
        String name
        Laminate meta
        T input

        OneToOneCallable(History log, String name, Laminate meta, T input) {
            this.log = log
            this.name = name
            this.meta = meta
            this.input = input
        }

        R execute(Closure<R> closure) {
            Closure<R> rehydrated = closure.rehydrate(this, null, null);
            rehydrated.setResolveStrategy(Closure.DELEGATE_ONLY);
            return rehydrated.call();
        }
    }
}
