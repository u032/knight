package wiki.chess.plugins

import com.google.firebase.cloud.FirestoreClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.config
import wiki.chess.enums.Country
import wiki.chess.enums.Role
import wiki.chess.httpClient
import wiki.chess.models.DiscordUser

fun Application.configureSecurity() {
    install(Authentication) {
        oauth("discord") {
            urlProvider = { "${config["FULL_HOST"]}/auth/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl = "https://discord.com/api/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = config["DISCORD_ID"],
                    clientSecret = config["DISCORD_SECRET"],
                    defaultScopes = listOf("identify")
                )
            }
            client = HttpClient(Apache)
        }
    }

    routing {
        route("/auth") {
            authenticate("discord") {
                get("/") {
                    call.respondRedirect(HttpRequestBuilder()
                        .url {
                            protocol = URLProtocol.HTTPS
                            host = "discord.com"
                            pathSegments = listOf("api", "oauth2", "authorize")
                            parameters.append("response_type", "code")
                            parameters.append("client_id", config["DISCORD_ID"])
                            parameters.append("scope", "identify")
                            parameters.append("redirect_uri", "${config["FULL_HOST"]}/auth/callback")
                            parameters.append("prompt", "none")
                        }.toString())
                }
                get("/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                    val token = principal?.accessToken.toString()
                    val discordUser: DiscordUser = httpClient.get("https://discord.com/api/users/@me") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }.body()

                    val usersCollection = FirestoreClient.getFirestore().collection("users")

                    val user = withContext(Dispatchers.IO) {
                        usersCollection
                            .document(discordUser.id)
                            .get().get()
                    }

                    if (!user.exists()) {
                        val data: Map<String, Any> = mapOf(
                            "id" to discordUser.id.toLong(),
                            "name" to discordUser.username,
                            "bio" to "Look at me, I'm new!",
                            "chessLink" to "",
                            "country" to Country.UN.name,
                            "email" to "",
                            "federation" to "",
                            "rating" to 0,
                            "role" to Role.USER.name,
                            "sex" to "",
                            "title" to ""
                        )
                        withContext(Dispatchers.IO) {
                            usersCollection.document(discordUser.id).set(data).get()
                        }
                    }

                    call.respondRedirect("${config["FRONTEND_CALLBACK_URL"]}?token=$token")
                }
            }
        }
    }
}
