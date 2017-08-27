package hep.dataforge.grind.workspace

import hep.dataforge.actions.ExecAction
import hep.dataforge.context.Context
import hep.dataforge.io.IOUtils
import hep.dataforge.meta.Laminate

import java.nio.ByteBuffer

/**
 *
 */
class ExecSpec {

    private Closure handleInput = {};

    private Closure handleOutput = { delegate.text };

    void input(@DelegatesTo(value = InputTransformer, strategy = Closure.DELEGATE_ONLY) Closure handleInput) {
        this.handleInput = handleInput
    }

    void output(@DelegatesTo(value = OutputTransformer, strategy = Closure.DELEGATE_ONLY) Closure handleOutput) {
        this.handleOutput = handleOutput
    }

    private class InputTransformer {
        String name;
        Object input;
        Laminate meta

        private ByteArrayOutputStream stream;

        ByteArrayOutputStream getStream() {
            if (stream == null) {
                stream = new ByteArrayOutputStream();
            }
            return stream
        }
    }

    private class OutputTransformer {
        Context context;
        Process process;
        Laminate meta;

        String getText() {
            return process.getText();
        }
    }

    private class GrindExecAction extends ExecAction {

        @Override
        protected ByteBuffer transformInput(String name, Object input, Laminate meta) {
            def inputTransformer = new InputTransformer(name: name, input: input, meta: meta);
            handleInput.rehydrate(inputTransformer, null, null);
            handleInput.setResolveStrategy(Closure.DELEGATE_ONLY);
            def res = handleInput.call();

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
        protected Object handleOutput(Context context, Process process, Laminate meta) {
            def outputTransformer = new OutputTransformer(context: context, process: process, meta: meta);
            handleOutput.rehydrate(outputTransformer, null, null);
            handleOutput.setResolveStrategy(Closure.DELEGATE_ONLY);
            return handleOutput.call();
        }
    }
}
