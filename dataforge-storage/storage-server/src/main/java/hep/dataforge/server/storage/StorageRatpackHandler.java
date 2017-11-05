package hep.dataforge.server.storage;

import freemarker.template.Template;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.server.ServerManager;
import hep.dataforge.server.ServletUtils;
import hep.dataforge.storage.api.*;
import hep.dataforge.storage.commons.JSONMetaWriter;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static hep.dataforge.server.storage.StorageRenderer.renderStorage;

/**
 * Created by darksnake on 13-Dec-15.
 */
public class StorageRatpackHandler implements Handler {

    private final ServerManager manager;
    private final Storage root;
    private Map<String, SoftReference<Loader>> cache = new ConcurrentHashMap<>();

    public StorageRatpackHandler(ServerManager manager, Storage root) {
        this.manager = manager;
        this.root = root;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        if (ctx.getRequest().getQuery().isEmpty()) {
            renderStorageTree(ctx, root);
        } else {
            String path = ctx.getRequest().getQueryParams().get("path");
            //quick-fix to work with root loaders

            Loader loader = null;
            if (cache.containsKey(path)) {
                loader = cache.get(path).get();
            }
            if (loader == null) {
                loader = root.provide(path, Loader.class).orElseThrow(() -> new NameNotFoundException(path));
                cache.put(path, new SoftReference<>(loader));
            }

            switch (loader.getType()) {
                case StateLoader.STATE_LOADER_TYPE:
                    renderStates(ctx, (StateLoader) loader);
                    return;
                case EventLoader.EVENT_LOADER_TYPE:
                    renderEvents(ctx, (EventLoader) loader);
                    return;
                case TableLoader.TABLE_LOADER_TYPE:
                    if ("pull".equals(ctx.getRequest().getQueryParams().get("action"))) {
                        new PointLoaderDataHandler((TableLoader) loader).handle(ctx);
                    } else {
                        renderPoints(ctx, (TableLoader) loader);
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

    protected void renderStorageTree(Context ctx, Storage storage) {
        try {
            storage.refresh();
            ctx.getResponse().contentType("text/html");
            Template template = ServletUtils.freemarkerConfig().getTemplate("Storage.ftl");

            StringBuilder b = new StringBuilder();
            String basePath = ctx.getRequest().getPath() + "?path=";
            renderStorage(b, basePath, storage);

            Map<String, Object> data = buildStorageData(ctx);
            data.put("storageName", storage.getName());
            data.put("content", b.toString());
            data.put("path", storage.getFullName().asArray());

            StringWriter writer = new StringWriter();
            template.process(data, writer);

            ctx.render(writer.toString());

        } catch (Exception ex) {
            LoggerFactory.getLogger(StorageRenderer.class).error("Error rendering storage tree");
            ctx.render(ex.toString());
        }
    }

    protected void defaultRenderLoader(Context ctx, Loader loader) {
        ctx.render("Loader view for loader " + loader.getName() + " not yet implemented");
    }


    protected void renderStates(Context ctx, StateLoader loader) {
        try {
            ctx.getResponse().contentType("text/html");
            Template template = ServletUtils.freemarkerConfig().getTemplate("StateLoader.ftl");

            Map<String, String> stateMap = new HashMap<>();
            for (String stateName : loader.getStateNames()) {
                stateMap.put(stateName, loader.getString(stateName));
            }

            Map<String, Object> data = buildLoaderData(ctx, loader);
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

    protected void renderObjects(Context ctx, ObjectLoader<?> loader) {
        defaultRenderLoader(ctx, loader);
    }

    protected MetaBuilder pointLoaderPlotOptions(TableLoader loader) {
        return new MetaBuilder("options")
                .putValue("width", "90%")
                .putValue("height", 500)
                .putValue("curveType", "function")
                .putNode(new MetaBuilder("explorer")
                        .putValues("actions", "dragToZoom", "rightClickToReset")
                );
    }

    protected void renderPoints(Context ctx, TableLoader loader) {
        try {
            ctx.getResponse().contentType("text/html");
//            Template template = Utils.freemarkerConfig().getTemplate("PointLoaderTemplate.ftl");
            Template template = ServletUtils.freemarkerConfig().getTemplate("PointLoader.ftl");

            Map<String, Object> data = buildLoaderData(ctx, loader);
            String plotParams = new JSONMetaWriter().writeString(pointLoaderPlotOptions(loader)).trim();
            data.put("plotParams", plotParams);
            //            data.put("data", loader.getIndex(valueName).pull(Value.of(from), Value.of(to), Integer.valueOf(maxItems)));

            StringWriter writer = new StringWriter();
            template.process(data, writer);

            ctx.render(writer.toString());
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error("Failed to render template", ex);
            ctx.render(ex.toString());
        }
    }

    private Map<String, Object> buildStorageData(Context ctx) {
        Map<String, Object> binding = manager.buildBasicData(ctx);


//            String valueName = ctx.getRequest().getQueryParams().getOrDefault("valueName", "timestamp");
//            String from = ctx.getRequest().getQueryParams().get("from");
//            String to = ctx.getRequest().getQueryParams().get("to");
//            String maxItems = ctx.getRequest().getQueryParams().getOrDefault("items", "250");

        String dataSourceStr = ctx.getRequest().getUri() + "&action=pull";

        binding.put("dataSource", dataSourceStr);
        binding.put("rootURL", ctx.getRequest().getPath());

        binding.put("updateInterval", 30);
        return binding;
    }

    private Map<String, Object> buildLoaderData(Context ctx, Loader loader) {
        Map<String, Object> data = buildStorageData(ctx);
        data.put("path", loader.getStorage().getFullName().asArray());
        data.put("loaderName", loader.getName());
        return data;
    }

}
