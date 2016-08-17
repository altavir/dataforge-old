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

    static Workspace buildWorkspace(File file, Class spec){
        return new GrindLauncher().from(file).with(spec).buildWorkspace();
    }

    static Workspace buildWorkspace(File file){
        return new GrindLauncher().from(file).buildWorkspace();
    }


    private Closure<? extends Reader> source = { new File("workspace.groovy").newReader() }
    private Class<? extends WorkspaceSpec> spec = WorkspaceSpec.class

    GrindLauncher from(File file) {
        this.source = { file.newReader() }
        return this;
    }

    GrindLauncher from(Closure<? extends Reader> readerSup) {
        this.source = readerSup
        return this;
    }

    GrindLauncher from(String str) {
        this.source = { new StringReader(str) }
        return this;
    }

    GrindLauncher with(Class<? extends WorkspaceSpec> specClass) {
        this.spec = specClass
        return this;
    }

//    GrindLauncher inside(Closure source) {
//
//    }

    /**
     * Create workspace builder using WorkspaceSpec
     * @param input
     * @return
     */
    Workspace.Builder getBuilder() {
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = spec.name
        def shell = new GroovyShell(this.class.classLoader, new Binding(), compilerConfiguration)
        Script script = shell.parse(source()) as WorkspaceSpec;
        return script.run() as Workspace.Builder;
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
