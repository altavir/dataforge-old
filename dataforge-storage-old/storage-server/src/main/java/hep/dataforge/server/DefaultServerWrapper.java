package hep.dataforge.server;

public class DefaultServerWrapper implements ServerWrapper<Object> {
    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    @Override
    public ServerObject wrap(ServerObject parent, Object object, String path) {
        return new SimpleServerObject(parent, path, chain -> {
            try {
                chain.prefix(path, subChain -> {
                    subChain.get(ctx -> {
                        ctx.render("Can't display object " + object);
                    });
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }
}
