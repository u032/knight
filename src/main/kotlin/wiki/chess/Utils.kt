package wiki.chess

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun HttpMessageBuilder.authorization(token: String) {
    this.headers.append(HttpHeaders.Authorization, token)
}

fun HttpMessageBuilder.bearerAuthorization(token: String) {
    authorization("Bearer $token")
}

val discordApi = HttpClient {
    defaultRequest {
        url("https://discord.com/api/v10/")
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}
