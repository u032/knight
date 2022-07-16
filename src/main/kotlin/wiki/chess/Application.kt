package wiki.chess

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import wiki.chess.plugins.configureHTTP
import wiki.chess.plugins.configureRouting
import java.io.FileInputStream

var dbNullable: Firestore? = null
val db: Firestore get() = dbNullable!!
val config = dotenv()

fun main() {
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream(config["ADMIN_SDK"])))
        .build()

    FirebaseApp.initializeApp(options)

    dbNullable = FirestoreClient.getFirestore()

    embeddedServer(Netty, port = config["PORT"].toInt()) {
        configureRouting()
        configureHTTP()
    }.start(wait = true)
}
