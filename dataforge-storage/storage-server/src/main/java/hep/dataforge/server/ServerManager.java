package hep.dataforge.server;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.description.ValueDef;
import javafx.beans.binding.BooleanBinding;
import javafx.util.Pair;
import org.jetbrains.annotations.Nullable;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerSpec;
import ratpack.server.ServerConfigBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A web server manager. Only one servlet is allowed per context
 * Created by darksnake on 12-May-17.
 */
@PluginDef(name = "server", group = "hep.dataforge", dependsOn = {"hep.dataforge:storage"}, info = "Storage servlet context plugin")
@ValueDef(name = "useRelativeAddress", type = "BOOLEAN", def = "true")
@ValueDef(name = "port", type = "NUMBER", def = "8337", info = "The port for the servlet")
public class ServerManager extends BasicPlugin implements ServerObject {

    /**
     * A server instance
     */
    private RatpackServer ratpack;

    /**
     * Customizable handlers
     */
    private Handler rootHandler; //TODO replace by full chaing configuration

    private List<ServerObject> bindings = new ArrayList<>();

    public BooleanBinding isStarted = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
            return ratpack != null;
        }
    };


    @Override
    public void attach(Context context) {
        super.attach(context);
        if (rootHandler == null) {
            rootHandler = new ContextRatpackHandler(this, context);
        }
    }

    public void startServer() throws Exception {
        if (ratpack != null) {
            throw new RuntimeException("Server already running");
        }


        ratpack = RatpackServer.start(this::update);
        isStarted.invalidate();
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
     * @param rootHandler
     */
    public void setRootHandler(Handler rootHandler) {
        this.rootHandler = rootHandler;
        reload();
    }

    /**
     * Add new ServerObject root and add it to main menu
     *
     * @param <T>
     */
    public <T> void bind(ServerObject sobj) {
        bindings.add(sobj);
        reload();
    }

    public void stopServer() {
        if (ratpack != null && ratpack.isRunning()) {
            try {
                ratpack.stop();
                ratpack = null;
            } catch (Exception ex) {
                getContext().getLogger().error("Failed to stop ratpack server", ex);
            }
        }
        isStarted.invalidate();
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
        if (isStarted.get()) {
            return ratpack.getScheme() + "://"
                    + ratpack.getBindHost() + ":"
                    + ratpack.getBindPort();
        } else {
            return "";
        }
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
        if (rootHandler != null) {
            chain.get(rootHandler);
        }
        ServerObject.super.updateChain(chain);
    }

    @Override
    public Stream<ServerObject> getChildren() {
        return bindings.stream();
    }
}
