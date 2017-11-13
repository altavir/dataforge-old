package hep.dataforge.server;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.description.ValueDef;
import javafx.beans.binding.BooleanBinding;
import javafx.util.Pair;
import org.jetbrains.annotations.Nullable;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerSpec;
import ratpack.server.ServerConfigBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hep.dataforge.values.ValueType.BOOLEAN;
import static hep.dataforge.values.ValueType.NUMBER;

/**
 * A web server manager. Only one servlet is allowed per context
 * Created by darksnake on 12-May-17.
 */
@PluginDef(name = "server", group = "hep.dataforge", dependsOn = {"hep.dataforge:storage"}, info = "Storage servlet context plugin")
@ValueDef(name = "useRelativeAddress", type = {BOOLEAN}, def = "true")
@ValueDef(name = "port", type = {NUMBER}, def = "8337", info = "The port for the servlet")
public class ServerManager extends BasicPlugin implements ServerObject {

    /**
     * A server instance
     */
    private RatpackServer ratpack;

    /**
     * Customizable handlers
     */
    private Action<Chain> rootHandler; //TODO replace by full chaing configuration

    private List<ServerObject> bindings = new ArrayList<>();

    public BooleanBinding isStartedProperty = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return ratpack != null;
        }
    };

    public boolean isStarted() {
        return isStartedProperty.get();
    }


    @Override
    public void attach(Context context) {
        super.attach(context);
        if (rootHandler == null) {
            rootHandler = chain -> chain.get(new ContextRatpackHandler(this, context));
        }
    }

    @Override
    public void detach() {
        try {
            close();
        } catch (Exception e) {
            getLogger().error("Can't close server manager", e);
        }
        super.detach();
    }

    public void startServer() throws Exception {
        if (ratpack != null) {
            throw new RuntimeException("Server already running");
        }


        ratpack = RatpackServer.start(this::update);
        isStartedProperty.invalidate();
    }

    private void update(RatpackServerSpec server) throws Exception {
        int port = getMeta().getInt("port");

        server.serverConfig((ServerConfigBuilder config) -> config
                .port(port) //TODO add custom address
                .findBaseDir());

        server.handlers((Chain chain) -> {
                    chain.files(fileHandlerSpec -> {
                        fileHandlerSpec.dir("public");
                    });
                    updateChain(chain);
                }
        );
    }

    private void reload() {
        if (ratpack != null) {
            try {
                ratpack.reload();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Set the handler for the index page
     *
     * @param action
     */
    public void setIndexPage(Action<Chain> action) {
        this.rootHandler = action;
        reload();
    }

    /**
     * Add new ServerObject root and add it to main menu
     */
    public void bind(ServerObject sobj) {
        bindings.add(sobj);
        reload();
    }

    public void stopServer() {
        if (ratpack != null && ratpack.isRunning()) {
            try {
                ratpack.stop();
                ratpack = null;
            } catch (Exception ex) {
                getLogger().error("Failed to stop ratpack server", ex);
            }
        }
        isStartedProperty.invalidate();
    }

    /**
     * List all registered top-level objects for navigation
     *
     * @return
     */
    public List<ServerObject> getBindings() {
        return bindings;
    }

    /**
     * Generate http link for server to use in external applications. Do not use it inside handlers
     *
     * @return
     */
    public String getLink() {
        if (ratpack != null) {
            return ratpack.getScheme() + "://"
                    + ratpack.getBindHost() + ":"
                    + ratpack.getBindPort();
        } else {
            return "";
        }
    }

    /**
     * Wrap a given object into a server object
     *
     * @param parent
     * @param object
     * @param path
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ServerObject wrap(ServerObject parent, Object object, String path) {
        ServiceLoader<ServerWrapper> loader = ServiceLoader.load(ServerWrapper.class);
        ServerWrapper wrapper = StreamSupport.stream(loader.spliterator(), false)
                .filter(it -> it.getType().isInstance(object)).findFirst()
                .orElseGet(DefaultServerWrapper::new);
        return wrapper.wrap(parent, object, path);
    }

    public Map<String, Object> buildBasicData(ratpack.handling.Context ctx) {
        Map<String, Object> binding = new HashMap<>();

        String serverAddress;
        if (meta().getBoolean("useRelativeAddress")) {
            serverAddress = "";
        } else {
            serverAddress = ServletUtils.getServerURL(ctx);
        }


        binding.put("homeURL", serverAddress);
        binding.put("navigation", getChildren().map(child -> new Pair<>(child.getPath(), child.getTitle())).collect(Collectors.toList()));

        return binding;

    }

    @Override
    public ServerManager getManager() {
        return this;
    }

    @Override
    public @Nullable ServerObject getParent() {
        return null;
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public void updateChain(Chain chain) {
        try {
            rootHandler.execute(chain);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply root chain", e);
        }
        ServerObject.super.updateChain(chain);
    }

    @Override
    public Stream<ServerObject> getChildren() {
        return bindings.stream();
    }

    @Override
    public void close() throws Exception {
        stopServer();
        for (ServerObject o : bindings) {
            o.close();
        }
    }
}
