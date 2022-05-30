package wiki.chess

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import wiki.chess.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost") {
        configureSessions()
        configureRouting()
        configureSecurity()
        configureHTTP()
    }.start(wait = true)

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .setDatabaseUrl("https://knight.firebaseio.com/")
        .build()
    FirebaseApp.initializeApp(options)
}

val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}
