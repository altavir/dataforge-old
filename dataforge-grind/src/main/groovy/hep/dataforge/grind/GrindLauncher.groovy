package hep.dataforge.grind

import groovy.transform.CompileStatic
import hep.dataforge.data.DataNode
import hep.dataforge.description.TextDescriptorFormatter
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.Workspace
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by darksnake on 04-Aug-16.
 */
@CompileStatic
class GrindLauncher {

    private Closure<? extends Reader> source = { new File("workspace.groovy").newReader() }
    private Class<? extends WorkspaceSpec> spec = WorkspaceSpec.class
    private PrintStream out = System.out;

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

    GrindLauncher withSpec(Class<? extends WorkspaceSpec> specClass) {
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
    private Workspace.Builder getBuilder() {
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

    private DataNode runInWorkspace(Workspace workspace, String taskName, Meta meta) {
        return workspace.runTask(taskName, meta).computeAll();
    }

    DataNode runTask(String taskName, Meta meta) {
        return runInWorkspace(buildWorkspace(), taskName, meta);
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
        return runInWorkspace(ws, taskName, taskMeta);
    }

    DataNode runTask(String taskName) {
        Workspace ws = buildWorkspace();
        Meta taskMeta = ws.hasMeta(taskName) ? ws.getMeta(taskName) : Meta.empty();
        return runInWorkspace(ws, taskName, taskMeta);
    }

    /**
     * Smart run method
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

    /**
     * Display a list of available tasks
     */
    def tasks() {
        Workspace ws = buildWorkspace();
        StringWriter writer = new StringWriter();

        TextDescriptorFormatter formatter = new TextDescriptorFormatter(new PrintWriter(writer, true));
        ws.getTasks().forEach {
            formatter.showDescription(it.name, it.descriptor);
        }
        writer.flush()
        return writer.toString();
    }

}
