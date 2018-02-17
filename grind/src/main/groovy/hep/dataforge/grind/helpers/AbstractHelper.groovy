package hep.dataforge.grind.helpers

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.MarkupBuilder

import java.lang.reflect.Method

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

    /**
     * get the list of all methods that need describing
     * @return
     */
    protected Collection<Method> listDescribedMethods(){
        return getClass().getDeclaredMethods()
                .findAll { it.isAnnotationPresent(MethodDescription) }
    }

    @Override
    Markup getHeader() {
        MarkupBuilder builder = getHelperDescription();

        def methods = listDescribedMethods()

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

        return builder.build();
    }
}
