package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Encapsulated
import hep.dataforge.context.Global
import hep.dataforge.grind.helpers.PlotHelper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Created by darksnake on 15-Dec-16.
 */

@CompileStatic
class GrindShell implements Encapsulated {

    private Context context;
    private Binding binding = new Binding();
    private final GroovyShell shell;

    GrindShell(Context context = Global.instance()) {
        this.context = context
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStaticStars("java.lang.Math");

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer);

        //define important properties
        binding.setProperty("context", context)
        binding.setProperty("plots", new PlotHelper(context))
        shell = new GroovyShell(getClass().classLoader, binding, configuration);
    }

    def bind(String key, Object value) {
        binding.setProperty(key, value)
    }

    @Override
    Context getContext() {
        return context;
    }

    synchronized Object eval(String expression) {
        Object res = shell.evaluate(expression);
        //remembering last answer
        if (res != null) {
            bind("res", res)
        };
        //TODO remember n last answers
        return res;
    }
}

