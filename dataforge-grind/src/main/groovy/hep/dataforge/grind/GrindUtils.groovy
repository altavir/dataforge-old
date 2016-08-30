package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.meta.MetaBuilder
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by darksnake on 04-Aug-16.
 */
@CompileStatic
class GrindUtils {

    public static MetaBuilder buildMeta(Closure cl) {
        def metaSpec = new GrindMetaBuilder()
        def metaExec = cl.rehydrate(metaSpec, this, this);
        metaExec.resolveStrategy = Closure.DELEGATE_ONLY;
        return metaExec() as MetaBuilder
    }

    public static MetaBuilder buildMeta(Map values, Closure cl) {
        return buildMeta(cl).update(values);
    }

    public static MetaBuilder buildMeta(String nodeName, Closure cl) {
        def metaSpec = new GrindMetaBuilder()
        metaSpec.invokeMethod(nodeName, cl) as MetaBuilder
    }

    public static MetaBuilder buildMeta(String nodeName, Map values, Closure cl) {
        return buildMeta(nodeName, cl).update(values);
    }

    public static MetaBuilder buildMeta(String input) {
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = DelegatingScript.class.name
        def shell = new GroovyShell(GrindUtils.class.classLoader, compilerConfiguration)
        DelegatingScript script = shell.parse(input) as DelegatingScript;
        GrindMetaBuilder builder = new GrindMetaBuilder();
        script.setDelegate(builder)
        return script.run() as MetaBuilder
    }
}
