package wiki.chess.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureSecurity() {
    install(Authentication) {
        oauth("discord") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl = "https://discord.com/api/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                clientId = "920294906008834058",
                    clientSecret = "DYLKhw3-Uun-I6Op8E1sPCbIE9LGh_sp",
                    defaultScopes = listOf("identify")
                )
            }
            client = HttpClient(Apache)
        }
    }

    routing {
        authenticate("discord") {
            get("login") {
                call.respondRedirect(
                    "https://discord.com/api/oauth2/authorize?response_type=code&client_id=920294906008834058&scope=identify&redirect_uri=http://localhost:8080/callback&prompt=none"
                )
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/hello")
            }
        }
    }
}
