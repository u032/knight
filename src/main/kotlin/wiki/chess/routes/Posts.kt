package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.*
import wiki.chess.enums.Role
import wiki.chess.services.PostService

fun Route.posts() {
    get {
        val limit = call.getQuery("limit")?.toIntOrNull() ?: return@get
        val before = call.getQuery("before", false)!!

        if (limit.validateIsNegative(call, HttpStatusCode.BadRequest, "Number must not be negative"))
            return@get

        call.respond(PostService.getPosts(limit, before))
    }

    get("/{id}") {
        val post = call.getPost("id") ?: return@get

        call.respond(post)
    }

    put("/create") {
        val collection = db.collection("posts")
        val user = call.getUser() ?: return@put
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

    put("/{id}/incrementVotes") {
        call.getUser() ?: return@put
        val post = call.getPost("id") ?: return@put

        PostService.incrementVotes(post)

        call.respond(HttpStatusCode.OK)
    }

    put("/{id}/decrementVotes") {
        call.getUser() ?: return@put
        val post = call.getPost("id") ?: return@put

        PostService.decrementVotes(post)

        call.respond(HttpStatusCode.OK)
    }

    delete("/{id}/delete") {
        val user = call.getUser() ?: return@delete
        val post = call.getPost("id") ?: return@delete

        if (user.id != post.author && user.role != Role.MOD && user.role != Role.ADMIN) {
            call.respond(HttpStatusCode.Forbidden)
            return@delete
        }

        PostService.deletePost(post)

        call.respond(HttpStatusCode.OK)
    }
}
