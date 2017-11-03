package hep.dataforge.server

import io.ktor.application.ApplicationCall


interface ServerExtension {
    val route: String;
    fun intercept(call: ApplicationCall)
}