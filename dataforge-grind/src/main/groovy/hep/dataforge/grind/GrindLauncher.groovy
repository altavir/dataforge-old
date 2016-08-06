package hep.dataforge.grind

import hep.dataforge.data.DataNode
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.Workspace
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by darksnake on 04-Aug-16.
 */
class GrindLauncher {

    private Closure source = { new File("workspace.groovy") }

    GrindLauncher from(Closure source) {
        this.source = source
        return this;
    }

    /**
     * Create workspace builder using WorkspaceSpec
     * @param input
     * @return
     */
    Workspace.Builder buildWorkspace(Object input) {
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = DelegatingScript.class.name
        def shell = new GroovyShell(this.class.classLoader, compilerConfiguration)
        def script = shell.parse(input);
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
        return buildWorkspace(source()).build();
    }

    DataNode runTask(String taskName, Meta meta) {
        return buildWorkspace().runTask(taskName, meta);
    }

    /**
     * Run task with given name and meta built from closure
     * @param taskName
     * @param metaClosure
     * @return
     */
    DataNode runTask(String taskName, Closure metaClosure) {
        return buildWorkspace().runTask(taskName, GrindUtils.buildMeta(metaClosure));
    }

    DataNode runTask(String taskName, String target) {
        Workspace ws = buildWorkspace();
        Meta taskMeta = ws.hasMeta(target);
        return ws.runTask(taskName, taskMeta);
    }

    DataNode runTask(String taskName) {
        Workspace ws = buildWorkspace();
        Meta taskMeta = ws.hasMeta(taskName) ? ws.getMeta(taskName) : Meta.empty();
        return ws.runTask(taskName, taskMeta);
    }

    DataNode run(String input) {
        if(!input.contains("(")&& !input.contains("{")){
            return runTask(input)
        }
        Meta meta = GrindUtils.buildMeta(input);
        return runTask(meta.getName(), meta);
    }

}
