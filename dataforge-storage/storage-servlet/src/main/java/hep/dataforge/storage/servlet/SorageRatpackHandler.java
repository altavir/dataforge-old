package hep.dataforge.storage.servlet;

import freemarker.template.Template;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.*;
import hep.dataforge.values.Value;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

/**
 * Created by darksnake on 13-Dec-15.
 */
public class SorageRatpackHandler implements Handler {

    private final Storage root;

    private final Map<String, SoftReference<Loader>> cache = new ConcurrentHashMap<>();

    public SorageRatpackHandler(Storage root) {
        this.root = root;
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
                    ctx.render("Event loader view not yet implemented");
                    return;
                case PointLoader.POINT_LOADER_TYPE:
                    renderPoints(ctx, (PointLoader) loader);
            }
        }
    }

    private void renderStorageTree(Context ctx, Storage storage) {
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

    private void renderStorage(StringBuilder b, Storage storage) throws StorageException {
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

    private void renderLoader(StringBuilder b, Loader loader) {
        String href = "/storage?path=" + loader.getFullPath();
        b.append(String.format("<li><a href=\"%s\">%s</a> (%s)</li>", href, loader.getName(), loader.getType()));
    }

    private void renderStates(Context ctx, StateLoader loader) {
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

    private void renderPoints(Context ctx, PointLoader loader) {
        try {
            ctx.getResponse().contentType("text/html");
            Template template = Utils.freemarkerConfig().getTemplate("PointLoaderTemplate.ftl");

            Map data = new HashMap(2);

            String from = ctx.getRequest().getQueryParams().get("from");
            String to = ctx.getRequest().getQueryParams().get("to");
            String maxItems = ctx.getRequest().getQueryParams().getOrDefault("items", "250");

            data.put("loaderName", loader.getName());
            data.put("data", loader.pull(Value.of(from), Value.of(to), Integer.valueOf(maxItems)));

            StringWriter writer = new StringWriter();
            template.process(data, writer);

            ctx.render(writer.toString());
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to render template", ex);
            ctx.render(ex.toString());
        }
    }
}
