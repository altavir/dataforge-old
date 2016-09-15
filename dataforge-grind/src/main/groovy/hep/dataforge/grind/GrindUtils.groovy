package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.workspace.Workspace
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by darksnake on 04-Aug-16.
 */
@CompileStatic
class GrindUtils {

    public static MetaBuilder buildMeta(@DelegatesTo(GrindMetaBuilder) Closure cl) {
        return buildMeta("", cl);
    }

    public static MetaBuilder buildMeta(Map values, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        return buildMeta(cl).update(values);
    }

    public static MetaBuilder buildMeta(String nodeName, @DelegatesTo(GrindMetaBuilder) Closure cl) {
        def metaSpec = new GrindMetaBuilder()
        def metaExec = cl.rehydrate(metaSpec, null, null);
        metaExec.resolveStrategy = Closure.DELEGATE_ONLY;
        metaSpec.invokeMethod(nodeName, metaExec) as MetaBuilder
    }

    public static MetaBuilder buildMeta(String nodeName, Map values, @DelegatesTo(GrindMetaBuilder) Closure cl) {
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

    static Workspace buildWorkspace(File file, Class spec) {
        return new GrindWorkspaceBuilder().from(file).withSpec(spec).buildWorkspace();
    }

    static Workspace buildWorkspace(File file) {
        return new GrindWorkspaceBuilder().from(file).buildWorkspace();
    }

    static Workspace buildWorkspace(String file) {
        return new GrindWorkspaceBuilder().from(file).buildWorkspace();
    }
}
