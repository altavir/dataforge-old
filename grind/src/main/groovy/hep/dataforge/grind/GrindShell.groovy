package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Encapsulated
import hep.dataforge.context.Global
import hep.dataforge.grind.helpers.GrindHelperFactory
import hep.dataforge.meta.Meta
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

        //Load all available helpers
        context.serviceStream(GrindHelperFactory).forEach {
            def helper = it.build(context, Meta.empty()) //TODO add configuration to the shell
            if (binding.hasProperty(helper.name)) {
                context.logger.warn("The helper with the name ${helper.name} already loaded into shell. Overriding.")
            }
            binding.setProperty(helper.name, helper);
        }
        shell = new GroovyShell(context.classLoader, binding, configuration);
    }

    def bind(String key, Object value) {
        binding.setProperty(key, value)
    }

    @Override
    Context getContext() {
        return context;
    }

    /**
     * remembering last answer
     * @param res
     * @return
     */
    private def rememberResult(Object res) {
        if (res != null) {
            bind("res", res)
        };
        //TODO remember n last answers
        return res;
    }

    /**
     * Evaluate string expression
     * @param expression
     * @return
     */
    synchronized def eval(String expression) {
        return rememberResult(shell.evaluate(expression))
    }

    /**
     * Evaluate a closure using shell bindings
     * @param cl
     * @return
     */
    synchronized def eval(Closure cl) {
        Closure script = cl.rehydrate(binding, null, null);
        script.resolveStrategy = Closure.DELEGATE_ONLY;
        return rememberResult(script.call())
    }
}

