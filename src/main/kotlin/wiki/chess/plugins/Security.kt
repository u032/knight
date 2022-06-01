package wiki.chess.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import wiki.chess.config

fun Application.configureSecurity() {
    install(Authentication) {
        oauth("discord") {
            urlProvider = { "http://localhost:8080/auth/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "discord",
                    authorizeUrl = "https://discord.com/api/oauth2/authorize",
                    accessTokenUrl = "https://discord.com/api/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = config.id,
                    clientSecret = config.secret,
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
                    call.respondRedirect(config.authUrl)
                }
                get("/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                    call.respondRedirect("http://localhost/test/success.html?token=${principal?.accessToken.toString()}")
                }
            }
        }
    }
}
