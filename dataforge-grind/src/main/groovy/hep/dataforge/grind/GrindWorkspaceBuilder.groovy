package hep.dataforge.grind

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.data.DataNode
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.utils.ContextMetaFactory
import hep.dataforge.workspace.Workspace
import org.codehaus.groovy.control.CompilerConfiguration

import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * A wrapper class to dynamically load and update workspaces from configuration
 * Created by darksnake on 04-Aug-16.
 */
class GrindWorkspaceBuilder {

    private Closure<? extends Reader> source = { new File("workspace.groovy").newReader() }
    /**
     * Specification builder. Used for custom DSL extensions
     */
    private ContextMetaFactory<? extends WorkspaceSpec> specification = { context, meta -> new WorkspaceSpec(context) };

    /**
     * A script to startup workspace builder
     */
    private Consumer<Workspace.Builder> startup = { b -> }

    private String cachedScript;
    private Workspace.Builder cachedBuilder;

    Context parentContext;

    GrindWorkspaceBuilder(Context parentContext = Global.instance()) {
        this.parentContext = parentContext
    }

    /**
     * Read configuration from file
     * @param file
     * @return
     */
    GrindWorkspaceBuilder read(File file) {
        this.source = { file.newReader() }
        return this;
    }

    /**
     * Read from file using parent context file locator
     * @param fileName
     * @return
     */
    GrindWorkspaceBuilder readFile(String fileName) {
        return read(parentContext.io().getFile(fileName));
    }

    /**
     * Read workspace configuration form Reader providing closure
     * @param readerSup
     * @return
     */
    GrindWorkspaceBuilder read(Closure<? extends Reader> readerSup) {
        this.source = readerSup
        return this;
    }

    /**
     * Read configuration form String
     * @param str
     * @return
     */
    GrindWorkspaceBuilder read(String str) {
        this.source = { new StringReader(str) }
        return this;
    }


    GrindWorkspaceBuilder startup(Consumer<Workspace.Builder> consumer) {
        this.startup = consumer;
        return this;
    }

//    GrindWorkspaceBuilder startup(Closure cl) {
//        this.startup = { builder ->
//            def script = cl.rehydrate(builder,null,null);
//            script.setResolveStrategy(Closure.DELEGATE_ONLY);
//            script.run();
//        }
//        return this;
//    }

    void setSpecification(ContextMetaFactory<? extends WorkspaceSpec> specification) {
        this.specification = specification
    }

    /**
     * Create workspace builder using WorkspaceSpec. Use existing workspace if script not changed
     * @param input
     * @return
     */
    protected Workspace.Builder getBuilder() {
        def scriptText = source().text
        if (scriptText == cachedScript) {
            return cachedBuilder
        } else {
            def compilerConfiguration = new CompilerConfiguration()
            compilerConfiguration.scriptBaseClass = DelegatingScript.class.name;
            def shell = new GroovyShell(this.class.classLoader, new Binding(), compilerConfiguration)
            DelegatingScript script = shell.parse(scriptText) as DelegatingScript;
            WorkspaceSpec spec = specification.build(parentContext, Meta.empty());
            script.setDelegate(spec);
            script.run()
            def res = spec.build();
            startup.accept(res);
            cachedScript = scriptText;
            cachedBuilder = res;
            return res;
        }
    }

    /**
     * Build workspace using default file location
     * @return
     */
    Workspace build() {
        return getBuilder().build();
    }

    private DataNode runInWorkspace(Workspace workspace, String taskName, Meta meta) {
        return workspace.runTask(taskName, meta).computeAll();
    }

    DataNode runTask(String taskName, Meta meta) {
        return runInWorkspace(build(), taskName, meta);
    }

    /**
     * Run task with given name and meta built from action
     * @param taskName
     * @param metaClosure
     * @return
     */
    DataNode runTask(String taskName, Map values, @DelegatesTo(GrindMetaBuilder) Closure metaClosure) {
        return build().runTask(taskName, Grind.buildMeta(values, metaClosure));
    }

    private Meta transformTarget(String target) {
        Workspace ws = build();
        Meta targetMeta = Grind.parseMeta(target);

        if (ws.hasTarget(targetMeta.name)) {
            return new Laminate(ws.getTarget(targetMeta.name), targetMeta);
        } else {
            ws.context.logger.warn("Target with name {} not found in the workspace. Using default target.", targetMeta.name);
            return targetMeta;
        }
    }

    DataNode runTask(String taskName, String target) {
        Workspace ws = build();
        return runInWorkspace(ws, taskName, transformTarget(target));
    }

    DataNode runTask(String taskName) {
        Workspace ws = build();
        return runInWorkspace(ws, taskName, transformTarget(taskName));
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
        Meta meta = Grind.parseMeta(input);
        return runTask(meta.getName(), meta);
    }

    def methodMissing(String name, Object args) {
        String str = args.getClass().isArray() ? ((Object[]) args).join(" ") : args.toString()
        return runTask(name, str)
    }

    /**
     * Display a list of available tasks
     */
    def getTasks() {
        return build().tasks.collect(Collectors.toList())
//        Workspace ws = build();
//        StringWriter writer = new StringWriter();
//
//        TextDescriptorFormatter formatter = new TextDescriptorFormatter(new PrintWriter(writer, true));
//        ws.getTasks().forEach {
//            formatter.showDescription(it.name, it.descriptor);
//        }
//        writer.flush()
//        return writer.toString();
    }


    def getTargets() {
        return build().targets.collect(Collectors.toList())
//        Workspace ws = build();
//        StringWriter writer = new StringWriter();
//
//        ws.getTargets().forEach {
//            writer.println(it)
//        }
//
//        writer.flush()
//        return writer.toString();
    }

    def clean() {
        build().clean()
    }

}
