package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.enums.HttpError
import wiki.chess.enums.Role
import wiki.chess.services.PostService
import wiki.chess.services.UserService
import wiki.chess.validateHasLength
import wiki.chess.validateIsNull

fun Route.posts() {
    get("/get/all") {
        call.respond(PostService.getAllPosts())
    }
    get("/get/{id}") {
        val postId = call.parameters["id"].validateIsNull(call, HttpError.ID_PARAM) ?: return@get

        val post = PostService.getPostById(postId).validateIsNull(call, HttpError.POST_NOT_FOUND) ?: return@get

        call.respond(post)
    }
    put("/create") {
        val collection = db.collection("posts")
        val user = UserService.getUser(call) ?: return@put
        val content = call.receiveText()

        content.validateHasLength(call, min = 8) ?: return@put

        val id = withContext(Dispatchers.IO) { collection.get().get() }.documents.size + 1
        val data: Map<String, Any> = mapOf(
            "content" to content,
            "author" to user.id,
            "date" to System.currentTimeMillis() / 1000L
        )

        withContext(Dispatchers.IO) {
            collection.document(id.toString()).set(data).get()
        }

        call.respond(HttpStatusCode.OK)
    }
    put("/votes/increment/{id}") {
        val postId = call.parameters["id"].validateIsNull(call, HttpError.ID_PARAM) ?: return@put
        UserService.getUser(call) ?: return@put
        val post = PostService.getPostById(postId).validateIsNull(call, HttpError.POST_NOT_FOUND) ?: return@put

        PostService.incrementVotes(post)

        call.respond(HttpStatusCode.OK)
    }
    put("/votes/decrement/{id}") {
        val postId = call.parameters["id"].validateIsNull(call, HttpError.ID_PARAM) ?: return@put
        UserService.getUser(call) ?: return@put
        val post = PostService.getPostById(postId).validateIsNull(call, HttpError.POST_NOT_FOUND) ?: return@put

        PostService.decrementVotes(post)

        call.respond(HttpStatusCode.OK)
    }
    delete("/delete/{id}") {
        val postId = call.parameters["id"].validateIsNull(call, HttpError.ID_PARAM) ?: return@delete

        val user = UserService.getUser(call) ?: return@delete

        val post = PostService.getPostById(postId).validateIsNull(call, HttpError.POST_NOT_FOUND) ?: return@delete

        if (user.id != post.author && user.role != Role.MOD && user.role != Role.ADMIN) {
            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            return@delete
        }

        PostService.deletePost(post)

        call.respond(HttpStatusCode.OK)
    }
}
