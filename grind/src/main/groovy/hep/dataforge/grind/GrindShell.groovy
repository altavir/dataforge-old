package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.context.Context
import hep.dataforge.context.Encapsulated
import hep.dataforge.context.Global
import hep.dataforge.description.NodeDef
import hep.dataforge.description.NodeDefs
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValuesDefs
import hep.dataforge.grind.helpers.GrindHelperFactory
import hep.dataforge.meta.Meta
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Created by darksnake on 15-Dec-16.
 */

@CompileStatic
@ValuesDefs([
        @ValueDef(name = "import.utils", multiple = true, info = "The set of (full qualified) names of utility classes that should be imported into the shell"),
        @ValueDef(name = "import.package", multiple = true, info = "The set of (full qualified) package names to be fully imported"),
        @ValueDef(name = "import.classes", multiple = true, info = "The list of class names to be imported")
])

@NodeDefs([
        @NodeDef(name = "import", info = "Import customization"),
        @NodeDef(name = "import.one", multiple = true, info = "A single import. Can contain alas. If field is present, then using static import")
])
class GrindShell implements Encapsulated {

    private Context context;
    private ShellBinding binding = new ShellBinding();
    private final GroovyShell shell;

    GrindShell(Context context = Global.instance(), Meta meta = Meta.empty()) {
        this.context = context
        ImportCustomizer importCustomizer = new ImportCustomizer();

        //adding package import
        importCustomizer.addStarImports(meta.getStringArray("import.package") { new String[0] })
        //adding static imports
        importCustomizer.addStaticStars(
                meta.getStringArray("import.utils") {
                    ["java.lang.Math", "hep.dataforge.grind.Grind"] as String[]
                }
        )
        //adding regular imports
        importCustomizer.addImports(meta.getStringArray("import.classes") { new String[0] });
        //add import with aliases
        meta.getMetaList("import.one").each {
            if (it.hasValue("field")) {
                importCustomizer.addStaticImport(it.getString("alias", (String) null), it.getString("name"), it.getString("field"));
            } else {
                importCustomizer.addImport(it.getString("alias", (String) null), it.getString("name"));
            }
        }

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.addCompilationCustomizers(importCustomizer);

        //define important properties
        binding.setInternal("context", context)

        //Load all available helpers
        context.serviceStream(GrindHelperFactory).forEach {
            def helper = it.build(context, meta.getMetaOrEmpty(it.name))
            if (binding.internals.containsKey(it.name)) {
                context.logger.warn("The helper with the name ${it.name} already loaded into shell. Overriding.")
            }
            binding.setInternal(it.name, helper);
        }
        shell = new GroovyShell(context.classLoader, binding, configuration);
    }

    def bind(String key, Object value) {
        binding.setVariable(key, value)
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

    /**
     *  A shell binding with pre-defined immutable internal properties
     */
    private class ShellBinding extends Binding {
        private Map<String, ?> internals = new HashMap<>();

        void setInternal(String key, Object obj) {
            this.internals.put(key, obj);
        }

        @Override
        void setVariable(String propertyName, Object newValue) {
            if (internals.containsKey(propertyName)) {
                getContext().getLogger().error("The variable name ${propertyName} is occupied by internal object. It won't be accessible from the shell.")
            }
            super.setVariable(propertyName, newValue)
        }

        @Override
        Object getVariable(String propertyName) {
            if (internals.containsKey(propertyName)) {
                return internals.get(propertyName);
            } else {
                return super.getVariable(propertyName)
            }
        }


    }
}

