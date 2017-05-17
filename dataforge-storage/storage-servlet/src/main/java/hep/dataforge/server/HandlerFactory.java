package hep.dataforge.server;

import ratpack.handling.Handler;

import java.util.function.BiFunction;

/**
 * Created by darksnake on 15-May-17.
 */
public interface HandlerFactory<T> extends BiFunction<ServerManager, T, Handler> {
}
