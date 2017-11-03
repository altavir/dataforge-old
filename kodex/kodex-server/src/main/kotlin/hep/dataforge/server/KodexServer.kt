package hep.dataforge.server

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import io.ktor.application.install
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine


class KodexServer : Metoid, ContextAware {

    var engine: NettyApplicationEngine? = null;

    override fun meta(): Meta {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContext(): Context {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun start(){
        engine = embeddedServer(factory = Netty, port = meta.getInt("port", 8336)) {
            install(Routing) {
                get("status") {
                }
            }
        }.start()
    }

    fun stop(){
        engine?.stop(gracePeriod = 5000)
    }


}


fun main(args: Array<String>) {
    KodexServer().start()
}