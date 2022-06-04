package wiki.chess.routes

import com.google.firebase.cloud.FirestoreClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.httpClient
import wiki.chess.models.DiscordUser
import wiki.chess.models.Post
import wiki.chess.models.User

fun Route.posts() {
    val fsclient = FirestoreClient.getFirestore()

    route("/posts") {
        post("/create") {
            val token = call.request.headers[HttpHeaders.Authorization]
            val collection = fsclient.collection("posts")

            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "401")
                return@post
            }


            val discordUser: DiscordUser = httpClient.get("https://discord.com/api/users/@me") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()

            val user = withContext(Dispatchers.IO) {
                fsclient.collection("users").document(discordUser.id).get().get()
            }.toObject(User::class.java)


            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@post
            }

            val content = call.receiveText()
            if (content.length < 3) {
                call.respond(HttpStatusCode.NoContent, "204")
                return@post
            }
            val id = collection.listDocuments().count() + 1;
            val timestamp = System.currentTimeMillis() / 1000L;
            val data: Map<String, Any> = mapOf(
                "id" to id,
                "content" to content,
                "author" to discordUser.id,
                "date" to timestamp
            )
            withContext(Dispatchers.IO) {
                collection.document(id.toString()).set(data).get()
            }

            call.respond(200)
        }
        delete("/delete/{id}") {
                val token = call.request.headers[HttpHeaders.Authorization]
                val postId = call.parameters["id"]

                if (token == null) {
                    call.respond(HttpStatusCode.Unauthorized, "401")
                    return@delete
                }

                val discordUser: DiscordUser = httpClient.get("https://discord.com/api/users/@me") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }.body()

                val user = withContext(Dispatchers.IO) {
                    fsclient.collection("users").document(discordUser.id).get().get()
                }.toObject(User::class.java)


                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@delete
                }

                val post = withContext(Dispatchers.IO) {
                    fsclient.collection("posts").document(postId.toString()).get().get()
                }.toObject(Post::class.java)

                if (post == null) {
                    call.respond(HttpStatusCode.NotFound, "Post not found")
                    return@delete
                }
                if (!discordUser.id.equals(post.author) && !user.role.equals("MOD")) {
                    call.respond(HttpStatusCode.Unauthorized, "401")
                    return@delete
                }
                fsclient.collection("posts").document(postId.toString()).delete()
                call.respond(200)
            }

            get("/get/{id}") {
                val postId = call.parameters["id"]
                if(postId == null) {
                    call.respond(HttpStatusCode.NotFound, "Post not found")
                    return@get
                }

                val post = withContext(Dispatchers.IO) {
                    fsclient.collection("posts").document(postId).get().get()
                }.toObject(Post::class.java)


                if (post == null) {
                    call.respond(HttpStatusCode.NotFound, "Post not found")
                    return@get
                }

                call.respond(post)
            }
        }
    }
