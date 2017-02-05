package hep.dataforge.grind

import hep.dataforge.context.Context
import hep.dataforge.context.Encapsulated
import hep.dataforge.context.Global
import hep.dataforge.data.Data
import hep.dataforge.data.DataNode
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValuesDefs
import hep.dataforge.grind.helpers.PlotHelper
import hep.dataforge.meta.Meta
import hep.dataforge.meta.SimpleConfigurable
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.util.function.Consumer

/**
 * Created by darksnake on 15-Dec-16.
 */
@ValuesDefs([
    @ValueDef(name = "evalClosures", type = "BOOLEAN", def = "true", info = "Automatically replace closures by their results"),
    @ValueDef(name = "evalData", type = "BOOLEAN", def = "false", info = "Automatically replace data by its value")
])
class GrindShell extends SimpleConfigurable implements Encapsulated {

    private Context context;
    private Binding binding = new Binding();
    private final GroovyShell shell;
    private Set<Hook> hooks = new HashSet<>();

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

    def <T> void hook(Class<T> type, Consumer<T> con) {
        hooks.add(new Hook(type, con));
    }

    def hook(Consumer<?> con) {
        hooks.add(new Hook(java.lang.Object, con));
    }

    @Override
    Context getContext() {
        return context;
    }

    synchronized Object eval(String expression) {
        Object res = shell.evaluate(expression);
        //remembering last answer
        bind("res", res);
        //TODO remember n last answers
        return postEval(res);
    }

    /**
     * Post evaluate result. Compute lazy data and use smart data visualization
     * @param res
     * @return
     */
    protected Object postEval(Object res) {
        if (getConfig().getBoolean("evalClosures", true) && res instanceof Closure) {
            res = res.call()
        }

        if (getConfig().getBoolean("evalData", false) && res instanceof Data) {
            res = res.get();
        }

        if (res instanceof DataNode) {
            res.dataStream().forEach { postEval(it) };
        }

        hooks.each {
            it.accept(res);
        }

        return res;
    }

    @Override
    protected void applyConfig(Meta config) {

    }

    /**
     * A consumer that applies only to given type
     * @param < T >
     */
    class Hook<T> {
        private final Class<T> type;
        private final Consumer<T> consumer;

        Hook(Class<T> type, Consumer<T> consumer) {
            this.type = type
            this.consumer = consumer
        }

        void accept(Object t) {
            if (type.isInstance(t)) {
                consumer.accept(t as T);
            }
        }
    }
}

