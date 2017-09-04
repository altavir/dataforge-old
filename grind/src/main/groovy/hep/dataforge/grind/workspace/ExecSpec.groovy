package hep.dataforge.grind.workspace

import groovy.transform.TupleConstructor
import hep.dataforge.actions.Action
import hep.dataforge.context.Context
import hep.dataforge.io.IOUtils
import hep.dataforge.io.markup.Markedup
import hep.dataforge.io.markup.SimpleMarkupRenderer
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta

import java.nio.ByteBuffer
import java.util.function.Function

/**
 * A specification for system exec task
 *
 */
class ExecSpec {

    /**
     * A task input handler. By default ignores input object.
     */
    private Closure handleInput = Closure.IDENTITY;

    /**
     * Handle task output. By default returns the output as text.
     */
    private Closure handleOutput = Closure.IDENTITY;

    /**
     * Build command line
     */
    private Closure cliTransform = Closure.IDENTITY;

    private String actionName = "exec";

    void input(@DelegatesTo(value = InputTransformer, strategy = Closure.DELEGATE_ONLY) Closure handleInput) {
        this.handleInput = handleInput
    }

    void output(@DelegatesTo(value = OutputTransformer, strategy = Closure.DELEGATE_ONLY) Closure handleOutput) {
        this.handleOutput = handleOutput
    }

    void cli(@DelegatesTo(value = CLITransformer, strategy = Closure.DELEGATE_ONLY) Closure cliTransform) {
        this.cliTransform = cliTransform
    }

    void name(String name) {
        this.actionName = name;
    }

    Action build() {
        return new GrindExecAction();
    }

    @TupleConstructor
    private class InputTransformer {
        final String name;
        final Object input;
        final Laminate meta

        private ByteArrayOutputStream stream;

        InputTransformer(String name, Object input, Laminate meta) {
            this.name = name
            this.input = input
            this.meta = meta
        }

        ByteArrayOutputStream getStream() {
            if (stream == null) {
                stream = new ByteArrayOutputStream();
            }
            return stream
        }

        def print(Object obj) {
            getStream().print(obj)
        }

        def println(Object obj) {
            getStream().println(obj)
        }

        def printf(String format, Object... args) {
            getStream().printf(format, args)
        }
    }

    @TupleConstructor
    private class OutputTransformer {
        /**
         * The name of the data
         */
        final String name;
        /**
         * Context for task execution
         */
        final Context context;
        /**
         * The system process for external task
         */
        final Process process;
        /**
         * task configuration
         */
        final Laminate meta;

        OutputTransformer(Context context, Process process, String name, Laminate meta) {
            this.name = name
            this.context = context
            this.process = process
            this.meta = meta
        }

        /**
         * Transformation of result
         */
        Function<String, ?> transform;

        private OutputStream outputStream;

        private Closure cl;


        void eval(Function<String, ?> transform) {
            this.transform = transform
        }

        /**
         * Get the output of the external task as text
         * @return
         */
        String getText() {
            return process.getText()
        }

        /**
         * Get the result using given transformation. If transformation not provided use text ouput of the task
         * @return
         */
        Object getResult() {
            return transform ? transform.apply(text) : text;
        }

        def redirect() {
            cl = {
                context.logger.debug("Redirecting process output to default task output")
                process.consumeProcessOutputStream(context.io().out(actionName, name))
            }
        }

        def redirect(String stage = "", String name) {
            cl = { process.consumeProcessOutputStream(context.io().out(stage, name)) }
        }

        /**
         * Create task output (not result)
         * @return
         */
        OutputStream getStream() {
            if (stream == null) {
                outputStream = context.io().out(actionName, name)
            }
            return stream
        }

        /**
         * Print something to default task output
         * @param obj
         * @return
         */
        def print(Object obj) {
            getStream().print(obj)
        }

        def println(Object obj) {
            getStream().println(obj)
        }

        def printf(String format, Object... args) {
            getStream().printf(format, args)
        }

        /**
         * Render a markedup object into default task output
         * @param markedup
         * @return
         */
        def render(Markedup markedup) {
            new SimpleMarkupRenderer(getStream()).render(markedup.markup())
        }

        private def transform() {
            if (cl != null) {
                cl.call()
            }
        }
    }

    @TupleConstructor
    private class CLITransformer {
        final Context context
        final String name
        final Meta meta


        String executable = ""
        List<String> cli = [];

        CLITransformer(Context context, String name, Meta meta) {
            this.context = context
            this.name = name
            this.meta = meta
        }

        /**
         * Apply inside parameters only if OS is windows
         * @param cl
         * @return
         */
        def windows(@DelegatesTo(CLITransformer) Closure cl){
            if (System.properties['os.name'].toLowerCase().contains('windows')) {
                this.with(cl)
            }
        }

        /**
         * Apply inside parameters only if OS is linux
         * @param cl
         * @return
         */
        def linux(@DelegatesTo(CLITransformer) Closure cl){
            if (System.properties['os.name'].toLowerCase().contains('linux')) {
                this.with(cl)
            }
        }

        def executable(String exec) {
            this.executable = executable
        }

        def append(String... commands) {
            cli.addAll(commands)
        }

        def argument(String key = "", Object obj) {
            String value;
            if (obj instanceof File) {
                value = obj.absoluteFile.toString();
            } else if (obj instanceof URL){
                value = new File(obj.toURI()).absoluteFile.toString();
            } else {
                value = obj.toString()
            }

            if(key){
                cli.add(key)
            }

            cli.add(value);
        }

        /**
         * Create
         * @return
         */
        private List<String> transform() {
            return [] +
                    meta.getString("exec", executable) +
                    cli +
                    Arrays.asList(meta.getStringArray("command", new String[0]))
        }
    }

    private class GrindExecAction extends ExecAction {

        @Override
        String getName() {
            return actionName;
        }

        @Override
        protected ByteBuffer transformInput(String name, Object input, Laminate meta) {
            def inputTransformer = new InputTransformer(name, input, meta);
            def handler = handleInput.rehydrate(inputTransformer, null, null);
            handler.setResolveStrategy(Closure.DELEGATE_ONLY);
            def res = handler.call();

            //If stream output is initiated, use it, otherwise, convert results
            if (inputTransformer.stream != null) {
                return ByteBuffer.wrap(inputTransformer.stream.toByteArray());
            } else if (res instanceof ByteBuffer) {
                return res;
            } else if (res != null) {
                return ByteBuffer.wrap(res.toString().getBytes(IOUtils.UTF8_CHARSET))
            } else {
                return null
            }
        }

        @Override
        protected Object handleOutput(Context context, Process process, String name, Laminate meta) {
            def outputTransformer = new OutputTransformer(context, process, name, meta);
            def handler = handleOutput.rehydrate(outputTransformer, null, null);
            handler.setResolveStrategy(Closure.DELEGATE_ONLY);
            handler.call();
            outputTransformer.transform()
            return outputTransformer.result;
        }

        @Override
        protected List<String> getCommand(Context context, String name, Meta meta) {
            def transformer = new CLITransformer(context, name, meta);
            def handler = cliTransform.rehydrate(transformer, null, null);
            handler.setResolveStrategy(Closure.DELEGATE_ONLY);
            handler.call()
            return transformer.transform().findAll{!it.isEmpty()}
        }
    }
}
