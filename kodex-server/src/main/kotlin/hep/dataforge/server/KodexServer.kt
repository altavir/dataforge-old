package hep.dataforge.server

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.utils.ContextMetaFactory
import io.ktor.application.install
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.util.concurrent.TimeUnit


class ServerInterceptor(val path: String, val builder: Route.() -> Unit)

typealias InterceptorFactory = ContextMetaFactory<ServerInterceptor>

class KodexServer(private val _context: Context, private val _meta: Meta) : Metoid, ContextAware {

    private var engine: NettyApplicationEngine? = null;
    private val interceptors: MutableList<InterceptorFactory> = ArrayList();

    override fun getMeta(): Meta {
        return _meta;
    }

    override fun getContext(): Context {
        return _context;
    }

    fun intercept(path: String, builder: Route.(Context, Meta) -> Unit): KodexServer {
        interceptors.add(InterceptorFactory { context, meta -> ServerInterceptor(path) { builder.invoke(this, context, meta) } })
        return this;
    }

    fun intercept(factory: InterceptorFactory): KodexServer {
        interceptors.add(factory)
        return this;
    }

    fun start() {
        engine = embeddedServer(factory = Netty, port = meta.getInt("port", 8336)) {
            install(Routing) {
                interceptors.map { it.build(context, meta) }.forEach { route(it.path, it.builder) }
            }
        }.start()
    }

    fun stop() {
        engine?.stop(gracePeriod = 5, timeout = 10, timeUnit = TimeUnit.SECONDS)
    }


}


fun main(args: Array<String>) {
    val meta = Meta.empty();
    KodexServer(Global.instance(), meta).start()
}