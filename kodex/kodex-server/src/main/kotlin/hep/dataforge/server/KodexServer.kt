package hep.dataforge.server

import io.ktor.application.install
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    embeddedServer(factory = Netty, port = 8080, watchPaths = listOf("BlogAppKt")){
        install(Routing){
            get("status"){

            }
        }
    }.start()
}