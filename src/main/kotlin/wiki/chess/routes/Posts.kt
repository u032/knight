package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.enums.Errors
import wiki.chess.enums.Role
import wiki.chess.getUser
import wiki.chess.services.PostService
import wiki.chess.validateHasLength
import wiki.chess.validateIsNull

fun Route.posts() {
    get("/get/all") {
        call.respond(PostService.getAllPosts())
    }
    get("/get/{id}") {
        val postId = call.parameters["id"].validateIsNull(call, Errors.ID_PARAM) ?: return@get

        val post = PostService.getPostById(postId)

        if (post == null) {
            call.respond(HttpStatusCode.NotFound, "Post not found")
            return@get
        }

        call.respond(post)
    }
    put("/create") {
        val collection = db.collection("posts")
        val user = getUser(call) ?: return@put
        val content = call.receiveText()

        content.validateHasLength(call, min = 8) ?: return@put

        val id = withContext(Dispatchers.IO) { collection.get().get() }.documents.size + 1
        val data: Map<String, Any> = mapOf(
            "id" to id,
            "content" to content,
            "author" to user.id,
            "date" to System.currentTimeMillis() / 1000L
        )

        withContext(Dispatchers.IO) {
            collection.document(id.toString()).set(data).get()
        }

        call.respond(HttpStatusCode.OK, "Post created")
    }
    delete("/delete/{id}") {
        val postId = call.parameters["id"].validateIsNull(call, Errors.ID_PARAM) ?: return@delete

        val user = getUser(call) ?: return@delete

        val post = PostService.getPostById(postId).validateIsNull(call, Errors.POST_NOT_FOUND) ?: return@delete

        if (user.id != post.author && user.role != Role.MOD && user.role != Role.ADMIN) {
            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            return@delete
        }

        PostService.deletePost(post)

        call.respond(HttpStatusCode.OK, "Post deleted")
    }
}
