package wiki.chess.plugins

import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import wiki.chess.routes.*
import java.time.Duration

fun Application.configureRouting() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    install(ContentNegotiation) {
        json()
    }

    routing {
        // WebSockets
        route("/notifications") { notificationRoutes() }

        // HTTPs
        route("/auth") { authRoutes() }
        route("/users") { userRoutes() }
        route("/posts") { postRoutes() }
        route("/mod") { modRoutes() }
    }
}
