package hep.dataforge.grind.actions

import groovy.transform.CompileStatic
import hep.dataforge.actions.OneToOneAction
import hep.dataforge.context.Context
import hep.dataforge.data.NamedData
import hep.dataforge.io.history.Chronicle
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.names.Name
import javafx.util.Pair

/**
 * Created by darksnake on 07-Aug-16.   
 */
@CompileStatic
class GrindPipe<T, R> extends OneToOneAction<T, R> {
    static GrindPipe build(String name, @DelegatesTo(PipeBuilder) Closure action) {
        return new GrindPipe(name, action.parameterTypes.first(), Object, action);
    }


    private final Class<T> inputType;
    private final Class<R> outputType;
    private final Closure action;

    GrindPipe(String name, Class<T> inputType = null, Class<R> outputType = null,
              @DelegatesTo(PipeBuilder) Closure action) {
        super(name)
        this.inputType = inputType
        this.outputType = outputType
        this.action = action
    }

    @Override
    Class<T> getInputType() {
        return inputType ?: super.inputType;
    }

    @Override
    Class<R> getOutputType() {
        return outputType ?: super.outputType;
    }

    @Override
    protected Pair<String, Meta> outputParameters(Context context, NamedData<? extends T> data, Meta actionMeta) {
        def pars = super.outputParameters(context, data, actionMeta)

        PipeBuilder<T, R> builder = new PipeBuilder<>(context, pars.key, pars.value.builder);
        action.setDelegate(builder)
        action.setResolveStrategy(Closure.DELEGATE_ONLY);
        action.call()

        return new Pair<>(builder.name, builder.meta);
    }

    @Override
    protected Object execute(Context context, String name, T input, Laminate meta) {
        PipeBuilder<T, R> builder = new PipeBuilder<>(context, name, meta.builder);
        action.setDelegate(builder)
        action.setResolveStrategy(Closure.DELEGATE_ONLY);
        action.call()

        Chronicle chronicle = context.getChronicle(Name.joinString(getName(), builder.name))
        return new ActionEnv(context, builder.name, builder.meta, chronicle)
                .execute(input, builder.result);
    }

    static class PipeBuilder<T, R> {
        final Context context;
        String name;
        MetaBuilder meta;

        //Class<T> type
        Closure<R> result = { it };

        PipeBuilder(Context context, String name, MetaBuilder meta) {
            this.context = context
            this.name = name
            this.meta = meta
        }

        def result(@DelegatesTo(value = ActionEnv, strategy = Closure.DELEGATE_FIRST) Closure<R> result) {
            this.result = result;
        }

    }
}
