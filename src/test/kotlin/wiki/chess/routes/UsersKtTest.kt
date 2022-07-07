package wiki.chess.routes

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import wiki.chess.authorization
import wiki.chess.enums.Country
import wiki.chess.enums.Federation
import wiki.chess.enums.Sex
import wiki.chess.models.User
import kotlin.test.Test
import kotlin.test.assertEquals

class UsersKtTest {
    @Test
    fun testGetUsersAll() = testApplication {
        HttpClient().get("http://localhost:8787/users/get/all").apply {
            println(this.bodyAsText())
            assertEquals(HttpStatusCode.OK, this.status)
        }
    }

    @Test
    fun testPutUsersUpdateMe() = testApplication {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        client.put("http://localhost:8787/users/update/@me") {
            authorization("")
            contentType(ContentType.Application.Json)
            setBody(User(name = "Krabi", bio = "Help me...", federation = Federation.FIDE, country = Country.FI, sex = Sex.FEMALE))
        }.apply {
            println(this.bodyAsText())
            assertEquals(HttpStatusCode.OK, this.status)
        }
    }
}
