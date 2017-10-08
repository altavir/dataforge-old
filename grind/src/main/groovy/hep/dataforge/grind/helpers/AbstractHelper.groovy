package hep.dataforge.grind.helpers

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.io.markup.MarkupBuilder

@CompileStatic
abstract class AbstractHelper implements GrindHelper {
    private final Context context;

    AbstractHelper(Context context) {
        this.context = context
    }

    @Override
    Context getContext() {
        return context
    }

    protected abstract MarkupBuilder getHelperDescription()

    @Override
    MarkupBuilder getHeader() {
        MarkupBuilder builder = getHelperDescription();

        def methods = getClass().getDeclaredMethods()
                .findAll { it.isAnnotationPresent(MethodDescription) }

        def descriptions = methods.collect {
            def desc = new MarkupBuilder().text(it.name, "magenta")

            if (it.parameters) {
                desc.text(" (")
                for (int i = 0; i < it.parameters.length; i++) {
                    def par = it.parameters[i]
                    desc.text(par.type.simpleName, "blue")
//                    desc.text(" ${par.name}")
                    if (i != it.parameters.length - 1) {
                        desc.text(", ")
                    }
                }
                desc.text(")")

            }

            desc.text(": ").text(it.getAnnotation(MethodDescription).value())
            return desc
        }

        if (descriptions) {
            builder.list(descriptions)
        }

        return builder;
    }
}
