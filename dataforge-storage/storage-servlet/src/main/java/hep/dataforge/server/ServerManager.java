package hep.dataforge.server;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorage;
import org.apache.commons.vfs2.FileObject;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerSpec;
import ratpack.server.ServerConfigBuilder;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A web server manager. Only one servlet is allowed per context
 * Created by darksnake on 12-May-17.
 */
@PluginDef(name = "server", group = "hep.dataforge", dependsOn = {"hep.dataforge:storage"}, info = "Storage servlet context plugin")
@ValueDef(name = "port", type = "NUMBER", def = "8337", info = "The port for the servlet")
public class ServerManager extends BasicPlugin {

    /**
     * A server instance
     */
    private RatpackServer ratpack;

    /**
     * Customizable handlers
     */
    private Handler rootHandler;
    private Map<String, Handler> handlers = new HashMap<>();

    /**
     * A registry remembering which path corresponds to which object
     */
    private Map<Object, String> paths = new HashMap<>();

    /**
     * Resolve the absolute path for a given object. Return '#' if object is not registered
     *
     * @param object
     * @return
     */
    public String resolveObject(Object object) {
        return paths.getOrDefault(object, "#");
    }

    /**
     * Register an object with the given path
     *
     * @param object
     * @param path
     */
    public synchronized void registerObject(Object object, String path) {
        this.paths.put(object, path);
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        if (rootHandler == null) {
            rootHandler = new ContextRatpackHandler(this, context);
        }
    }

    public void startSetver() throws Exception {
        if (ratpack != null) {
            throw new RuntimeException("Server already running");
        }
        int port = getMeta().getInt("port");

        ratpack = RatpackServer.start((RatpackServerSpec server) -> server
                .serverConfig((ServerConfigBuilder config) -> config
                        .port(port)
                        .findBaseDir()
                )
                .handlers((Chain chain) -> {
                            chain.files(fileHandlerSpec -> {
                                fileHandlerSpec.dir("public");
                            });
                            if (rootHandler != null) {
                                chain.get(rootHandler);
                            }
                            handlers.forEach(chain::get);
                        }
                )
        );
    }

    public void setRootHandler(Handler rootHandler) {
        this.rootHandler = rootHandler;
    }

    public void addHandler(String path, Handler handler) {
        this.handlers.put(path, handler);
    }

    public void addStorageHandler(String path, Storage storage) {
        addHandler(path, new StorageRatpackHandler(this, storage));
        paths.put(storage, path);
    }

    public void addStorageHandler(String path, File file) {
        addStorageHandler(path, FileStorage.connect(file, true, true));
    }

    public void addStorageHandler(String path, FileObject file) {
        addStorageHandler(path, FileStorage.connect(file, true, true));
    }

    public void stopServer() {
        if (ratpack != null && ratpack.isRunning()) {
            try {
                ratpack.stop();
                ratpack = null;
            } catch (Exception ex) {
                getContext().getLogger().error("Failed to stop ratpack servlet", ex);
            }
        }
    }

    public Collection<String> listHandlers() {
        return handlers.keySet();
    }


}
