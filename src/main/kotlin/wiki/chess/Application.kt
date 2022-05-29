package wiki.chess

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import wiki.chess.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSecurity()
        configureHTTP()
    }.start(wait = true)
}
