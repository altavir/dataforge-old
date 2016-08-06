package hep.dataforge.grind

import hep.dataforge.meta.MetaBuilder
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by darksnake on 04-Aug-16.
 */
class GrindUtils {

    public static MetaBuilder buildMeta(Closure cl){
        def metaSpec = new GrindMetaBuilder()
        def metaExec = cl.rehydrate(metaSpec, this, this);
        metaExec.resolveStrategy = Closure.DELEGATE_ONLY;
        return metaExec
    }

    public static MetaBuilder buildMeta(String input){
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = DelegatingScript.class.name
        def shell = new GroovyShell(this.class.classLoader, compilerConfiguration)
        def script = shell.parse(input);
        GrindMetaBuilder builder = new GrindMetaBuilder();
        script.setDelegate(builder)
        return script.run()
    }
}
