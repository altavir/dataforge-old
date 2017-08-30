package hep.dataforge.actions;

import hep.dataforge.context.Context;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaUtils;
import hep.dataforge.values.ValueType;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ValueDef(name = "command", required = true, multiple = true, info = "A command or a command line")
@ValueDef(name = "inheritIO", type = ValueType.BOOLEAN, def = "true", info = "Define if process should inherit IO from DataForge process")
@ValueDef(name = "timeout", type = ValueType.NUMBER, info = "The delay in milliseconds between end of output consumption and process force termination")
@ValueDef(name = "workDir", info = "The working directory for the process as defined by IOManager::getFile")
@NodeDef(name = "env", info = "Environment variables as a key-value pairs")
@NodeDef(name = "parameter", info = "The definition for command parameter")
public abstract class ExecAction<T, R> extends OneToOneAction<T, R> {

    @Override
    protected R execute(Context context, String name, T input, Laminate meta) {
        //setting up the process
        ProcessBuilder builder = new ProcessBuilder(getCommand(context, name, meta));

        //updating environment variables
        if (meta.hasMeta("env")) {
            MetaUtils.nodeStream(meta.getMeta("env")).forEach(envNode -> {
                builder.environment().put(envNode.getValue().getString("name", envNode.getKey()), envNode.getValue().getString("value"));
            });
        }

        // Setting working directory
        if (meta.hasValue("workDir")) {
            builder.directory(context.io().getFile(meta.getString("workDir")));
        }

        if (meta.getBoolean("inheritIO", true)) {
            builder.inheritIO();
        }

        Logger logger = getLogger(context, meta);

        try {
            logger.info("Starting process with command " + builder.command());
            Process process = builder.start();

            //sending input into process
            ByteBuffer bytes = transformInput(name, input, meta);
            if (bytes != null && bytes.limit() > 0) {
                logger.debug("The action input is transformed into byte array with length of " + bytes.limit());
                process.getOutputStream().write(bytes.array());
            }

            //consume process output
            logger.debug("Handling process output");


            R out = handleOutput(context, process, meta);

            if (process.isAlive()) {
                logger.debug("Starting listener for process end");
                //TODO somehow use context executor instead
                Thread thread = new Thread(() -> {
                    try {
                        if (meta.hasValue("timeout")) {
                            if (!process.waitFor(meta.getInt("timeout"), TimeUnit.MILLISECONDS)) {
                                process.destroyForcibly();
                            }
                        } else {
                            logger.info("Process finished with exit value " + process.waitFor());
                        }
                    } catch (Exception ex) {
                        logger.debug("Process failed to complete", ex);
                    }
                });
                thread.setName(name + "_listener");
                thread.start();
            } else {
                logger.info("Process finished with exit value " + process.exitValue());
            }

            return out;
        } catch (IOException e) {
            throw new RuntimeException("Process execution failed with error", e);
        }
    }

    protected List<String> getCommand(Context context, String name, Meta meta) {
        List<String> command = Arrays.asList(meta.getStringArray("command"));
        //TODO add smart parameter evaluation here
//        if(meta.hasMeta("parameter")){
//            meta.getMetaList("parameter").forEach(parMeta ->{
//
//            });
//        }
        return command;
    }

    protected abstract ByteBuffer transformInput(String name, T input, Laminate meta);

    protected abstract R handleOutput(Context context, Process process, Laminate meta);
}
