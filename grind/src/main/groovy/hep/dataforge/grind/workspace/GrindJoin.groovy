package hep.dataforge.grind.workspace

import hep.dataforge.actions.ManyToOneAction
import hep.dataforge.context.Context
import hep.dataforge.io.history.Chronicle
import hep.dataforge.meta.Laminate
import hep.dataforge.names.Name

class GrindJoin<T, R> extends ManyToOneAction<T, R> {
    private final String name;
    private final Closure<R> action;
    private final Map params;

    GrindJoin(Map params = [:], String name = "@dynamic",
              @DelegatesTo(value = ManyToOneCallable, strategy = Closure.DELEGATE_ONLY) Closure action) {
        this.name = name;
        this.action = action
        this.params = params
    }


    @Override
    protected R execute(Context context, String nodeName, Map<String, T> input, Laminate meta) {
        Chronicle chronicle = context.getChronicle(Name.joinString(getName(), nodeName))
        return new ManyToOneCallable<T, R>(context, chronicle, nodeName, meta, input).execute(action);
    }

    @Override
    String getName() {
        params.getOrDefault("name", name)
    }

    @Override
    Class getInputType() {
        params.getOrDefault("inputType", super.getInputType())
    }

    @Override
    Class getOutputType() {
        params.getOrDefault("outputType", super.getOutputType())
    }

    static class ManyToOneCallable<T, R> {
        Context context
        Chronicle log
        String name
        Laminate meta
        Map<String, T> input

        ManyToOneCallable(Context context, Chronicle log, String name, Laminate meta, Map<String, T> input) {
            this.context = context
            this.log = log
            this.name = name
            this.meta = meta
            this.input = input
        }

        R execute(Closure<R> closure) {
            Closure rehydrated = closure.rehydrate(this, null, null);
            rehydrated.setResolveStrategy(Closure.DELEGATE_ONLY);
            return rehydrated.call();
        }
    }
}
