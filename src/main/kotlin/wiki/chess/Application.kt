package wiki.chess

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import wiki.chess.plugins.configureHTTP
import wiki.chess.plugins.configureRouting
import wiki.chess.plugins.configureSecurity
import java.io.FileInputStream

var firestoreInstance: Firestore? = null
val db: Firestore get() = firestoreInstance!!
val config = dotenv()
val httpClient = HttpClient {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

fun main() {
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream(config["FIREBASE_ADMIN_SDK_KEY"])))
        .build()
    FirebaseApp.initializeApp(options)
    firestoreInstance = FirestoreClient.getFirestore()

    embeddedServer(Netty, port = config["PORT"].toInt(), host = config["HOST"]) {
        install(ContentNegotiation) {
            json()
        }

        configureRouting()
        configureSecurity()
        configureHTTP()
    }.start(wait = true)
}