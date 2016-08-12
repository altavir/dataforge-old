package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.data.DataNode
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.Workspace
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by darksnake on 04-Aug-16.
 */
@CompileStatic
class GrindLauncher {

    private Closure<? extends Reader> source = { new File("workspace.groovy").newReader() }

    GrindLauncher from(File file) {
        this.source = { file.newReader() }
        return this;
    }

    GrindLauncher from(Reader reader) {
        this.source = { reader }
        return this;
    }

    GrindLauncher from(String str) {
        this.source = { new StringReader(str) }
        return this;
    }

    GrindLauncher inside(Closure source) {

    }

    /**
     * Create workspace builder using WorkspaceSpec
     * @param input
     * @return
     */
    Workspace.Builder getBuilder() {
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = DelegatingScript.class.name
        def shell = new GroovyShell(this.class.classLoader, compilerConfiguration)
        DelegatingScript script = shell.parse(source()) as DelegatingScript;
        WorkspaceSpec spec = new WorkspaceSpec()
        script.setDelegate(spec)
        script.run()
        return spec.builder;
    }

    /**
     * Build workspace using default file location
     * @return
     */
    Workspace buildWorkspace() {
        return getBuilder().build();
    }

    DataNode runTask(String taskName, Meta meta) {
        return buildWorkspace().runTask(taskName, meta);
    }

    /**
     * Run task with given name and meta built from action
     * @param taskName
     * @param metaClosure
     * @return
     */
    DataNode runTask(String taskName, @DelegatesTo(GrindMetaBuilder) Closure metaClosure) {
        return buildWorkspace().runTask(taskName, GrindUtils.buildMeta(metaClosure));
    }

    DataNode runTask(String taskName, String target) {
        Workspace ws = buildWorkspace();
        Meta taskMeta = ws.hasMeta(target) ? ws.getMeta(target) : Meta.empty();
        return ws.runTask(taskName, taskMeta);
    }

    DataNode runTask(String taskName) {
        Workspace ws = buildWorkspace();
        Meta taskMeta = ws.hasMeta(taskName) ? ws.getMeta(taskName) : Meta.empty();
        return ws.runTask(taskName, taskMeta);
    }

    /**
     * Run
     * @param input
     * @return
     */
    DataNode run(String input) {
        if (!input.contains("(") && !input.contains("{")) {
            return runTask(input)
        }
        Meta meta = GrindUtils.buildMeta(input);
        return runTask(meta.getName(), meta);
    }

}
