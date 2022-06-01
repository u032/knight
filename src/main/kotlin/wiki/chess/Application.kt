package wiki.chess

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import wiki.chess.plugins.configureHTTP
import wiki.chess.plugins.configureRouting
import wiki.chess.plugins.configureSecurity
import java.io.FileInputStream

fun main() {
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream(config.firebase)))
        .build()
    FirebaseApp.initializeApp(options)

    embeddedServer(Netty, port = 8080, host = "localhost") {
        configureRouting()
        configureSecurity()
        configureHTTP()
    }.start(wait = true)
}
