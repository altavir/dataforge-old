package hep.dataforge.grind

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.data.DataNode
import hep.dataforge.description.TextDescriptorFormatter
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.Workspace
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by darksnake on 04-Aug-16.
 */
class GrindWorkspaceBuilder {

    private Closure<? extends Reader> source = { new File("workspace.groovy").newReader() }
    private Class<? extends WorkspaceSpec> spec = WorkspaceSpec.class
    private Context context = Global.instance();
    private String cachedScript;
    private Workspace.Builder cachedBuilder;

    GrindWorkspaceBuilder from(File file) {
        this.source = { file.newReader() }
        return this;
    }

    GrindWorkspaceBuilder from(Closure<? extends Reader> readerSup) {
        this.source = readerSup
        return this;
    }

    GrindWorkspaceBuilder from(String str) {
        this.source = { new StringReader(str) }
        return this;
    }

    GrindWorkspaceBuilder withSpec(Class<? extends WorkspaceSpec> specClass) {
        this.spec = specClass
        return this;
    }

    GrindWorkspaceBuilder withContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Create workspace builder using WorkspaceSpec. Use existing workspace if script not changed
     * @param input
     * @return
     */
    protected Workspace.Builder getBuilder() {
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = DelegatingScript.class.name;
        def shell = new GroovyShell(this.class.classLoader, new Binding(), compilerConfiguration)
        def scriptText = source().text
        if(scriptText == cachedScript){
            return cachedBuilder
        } else {
            DelegatingScript script = shell.parse(scriptText) as DelegatingScript;
            WorkspaceSpec spec = spec.newInstance();
            spec.parentContext = this.context;
            script.setDelegate(spec);
            script.run()
            def res = spec.build();
            cachedScript = scriptText;
            cachedBuilder = res;
            return res;
        }
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
        Meta taskMeta = ws.hasTarget(target) ? ws.getTarget(target) : Meta.empty();
        return runInWorkspace(ws, taskName, taskMeta);
    }

    DataNode runTask(String taskName) {
        Workspace ws = buildWorkspace();
        Meta taskMeta = ws.hasTarget(taskName) ? ws.getTarget(taskName) : Meta.empty();
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

    def methodMissing(String name, def args) {
        return runTask(name, args[0] as String)
    }

    /**
     * Display a list of available tasks
     */
    def getTasks() {
        Workspace ws = buildWorkspace();
        StringWriter writer = new StringWriter();

        TextDescriptorFormatter formatter = new TextDescriptorFormatter(new PrintWriter(writer, true));
        ws.getTasks().forEach {
            formatter.showDescription(it.name, it.descriptor);
        }
        writer.flush()
        return writer.toString();
    }


    def getTargets() {
        Workspace ws = buildWorkspace();
        StringWriter writer = new StringWriter();

        ws.getTargets().forEach {
            writer.println(it)
        }

        writer.flush()
        return writer.toString();
    }

    def clean() {
        buildWorkspace().clean()
    }

}
