package hep.dataforge.server;

import freemarker.template.Template;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static hep.dataforge.server.ServletUtils.getServerURL;

/**
 * Created by darksnake on 13-May-17.
 */
public class ContextRatpackHandler implements Handler {
    private final ServerManager manager;
    private final hep.dataforge.context.Context context;

    public ContextRatpackHandler(ServerManager manager, hep.dataforge.context.Context context) {
        this.manager = manager;
        this.context = context;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.getResponse().contentType("text/html");
        Template template = ServletUtils.freemarkerConfig().getTemplate("Context.ftl");


        Map<String, Object> binding = new HashMap<>();
        binding.put("homeURL",getServerURL(ctx));
        binding.put("navigation",manager.listHandlers());
        binding.put("contextName", context.getName());
        binding.put("properties", context.getProperties().entrySet());

        Map<String, String> plugins = context.pluginManager()
                .stream(true)
                .collect(Collectors.toMap(plugin -> {
                    if (plugin.getContext() == context) {
                        return plugin.getName();
                    } else {
                        return String.format("(%s) %s", plugin.getContext().getName(), plugin.getName());
                    }
                }, manager::resolveObject));

        binding.put("plugins", plugins.entrySet());

        StringWriter writer = new StringWriter();
        template.process(binding, writer);

        ctx.render(writer.toString());
    }
}
