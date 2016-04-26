package hep.dataforge.storage.servlet;

import freemarker.template.Template;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.*;
import hep.dataforge.values.Value;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.server.PublicAddress;

/**
 * Created by darksnake on 13-Dec-15.
 */
@SuppressWarnings("unchecked")
public class StorageRatpackHandler implements Handler {

    private final Storage root;
    private final Map<String, SoftReference<Loader>> cache;

    public StorageRatpackHandler(Storage root, Map<String, SoftReference<Loader>> cache) {
        this.root = root;
        if (cache == null) {
            this.cache = new ConcurrentHashMap<>();
        } else {
            this.cache = cache;
        }
    }

    public StorageRatpackHandler(Storage root) {
        this(root, null);
    }

    @Override
    public void handle(Context ctx) throws Exception {

        if (ctx.getRequest().getQuery().isEmpty()) {
            renderStorageTree(ctx, root);
        } else {
            String path = ctx.getRequest().getQueryParams().get("path");

            Loader loader = null;
            if (cache.containsKey(path)) {
                loader = cache.get(path).get();
            }
            if (loader == null) {
                loader = root.provide(path, Loader.class);
                cache.put(path, new SoftReference<>(loader));
            }

            switch (loader.getType()) {
                case StateLoader.STATE_LOADER_TYPE:
                    renderStates(ctx, (StateLoader) loader);
                    return;
                case EventLoader.EVENT_LOADER_TYPE:
                    renderEvents(ctx, (EventLoader) loader);
                    return;
                case PointLoader.POINT_LOADER_TYPE:
                    if (ctx.getRequest().getQueryParams().containsKey("tqx")) {
                        new PointLoaderVisualizationHandler((PointLoader) loader).handle(ctx);
                    } else {
                        renderPoints(ctx, (PointLoader) loader);
                    }
                    return;
                case ObjectLoader.OBJECT_LOADER_TYPE:
                    renderObjects(ctx, (ObjectLoader) loader);
                    return;
                default:
                    defaultRenderLoader(ctx, loader);
            }
        }
    }

    protected void defaultRenderLoader(Context ctx, Loader loader) {
        ctx.render("Loader view for loader " + loader.getName() + " not yet implemented");
    }

    protected void renderStorageTree(Context ctx, Storage storage) {
        try {
            ctx.getResponse().contentType("text/html");
            Template template = Utils.freemarkerConfig().getTemplate("StorageTemplate.ftl");

            StringBuilder b = new StringBuilder();
            renderStorage(b, storage);

            Map data = new HashMap(2);
            data.put("storageName", storage.getName());
            data.put("content", b.toString());

            StringWriter writer = new StringWriter();
            template.process(data, writer);

            ctx.render(writer.toString());

        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Error rendering storage tree");
            ctx.render(ex.toString());
        }
    }

    protected void renderStorage(StringBuilder b, Storage storage) throws StorageException {
        b.append("<div class=\"node\">\n");
        if (!storage.loaders().isEmpty()) {
            b.append("<div class=\"leaf\">\n"
                    + "<ul>");
            for (Loader loader : storage.loaders().values()) {
                renderLoader(b, loader);
            }
            b.append("</ul>"
                    + "</div>\n");
        }
        if (!storage.shelves().isEmpty()) {
            b.append("<ul>\n");
            for (Storage shelf : storage.shelves().values()) {
                b.append(String.format("<li><strong>+ %s</strong></li>%n", shelf.getName()));
                renderStorage(b, shelf);
            }
            b.append("</ul>");
        }
        b.append("</div>\n");
    }

    protected void renderLoader(StringBuilder b, Loader loader) {
        String href = "/storage?path=" + loader.getFullPath();
        b.append(String.format("<li><a href=\"%s\">%s</a> (%s)</li>", href, loader.getName(), loader.getType()));
    }

    protected void renderStates(Context ctx, StateLoader loader) {
        try {
            ctx.getResponse().contentType("text/html");
            Template template = Utils.freemarkerConfig().getTemplate("StateLoaderTemplate.ftl");

            Map<String, String> stateMap = new HashMap<>();
            for (String stateName : loader.getStateNames()) {
                stateMap.put(stateName, loader.getString(stateName));
            }

            Map data = new HashMap(2);
            data.put("loaderName", loader.getName());
            data.put("states", stateMap);

            StringWriter writer = new StringWriter();
            template.process(data, writer);

            ctx.render(writer.toString());
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to render template", ex);
            ctx.render(ex.toString());
        }
    }

    protected void renderEvents(Context ctx, EventLoader loader) {
        defaultRenderLoader(ctx, loader);
    }

    protected void renderObjects(Context ctx, ObjectLoader loader) {
        defaultRenderLoader(ctx, loader);
    }

    protected void renderPoints(Context ctx, PointLoader loader) {
        try {
            ctx.getResponse().contentType("text/html");
//            Template template = Utils.freemarkerConfig().getTemplate("PointLoaderTemplate.ftl");
            Template template = Utils.freemarkerConfig().getTemplate("PointLoaderView.ftl");

            Map data = new HashMap(2);

//            String valueName = ctx.getRequest().getQueryParams().getOrDefault("valueName", "timestamp");
//            String from = ctx.getRequest().getQueryParams().get("from");
//            String to = ctx.getRequest().getQueryParams().get("to");
//            String maxItems = ctx.getRequest().getQueryParams().getOrDefault("items", "250");
            String hostName;
            if (ctx.getServerConfig().getAddress() != null) {
                hostName = ctx.getServerConfig().getAddress().getHostAddress();
            } else {
                hostName = "localhost";
            }
            String serverAddres = "http://" + hostName + ":" + ctx.getServerConfig().getPort();
            data.put("dataSource", serverAddres + ctx.getRequest().getUri());
            data.put("loaderName", loader.getName());
//            data.put("data", loader.getIndex(valueName).pull(Value.of(from), Value.of(to), Integer.valueOf(maxItems)));

            StringWriter writer = new StringWriter();
            template.process(data, writer);

            ctx.render(writer.toString());
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to render template", ex);
            ctx.render(ex.toString());
        }
    }
}
