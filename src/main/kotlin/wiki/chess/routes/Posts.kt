package wiki.chess.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.enums.Role
import wiki.chess.getUser
import wiki.chess.models.Post
import wiki.chess.validateHasLength
import wiki.chess.validateIsNull

fun Route.posts() {
    get("/get") {
        val postsDocuments = withContext(Dispatchers.IO) {
            db.collection("posts").get().get().documents
        }

        val posts: ArrayList<Post> = ArrayList()

        postsDocuments.forEach { post ->
            posts.add(post.toObject(Post::class.java))
        }

        call.respond(posts)
    }
    get("/get/{id}") {
        val postId = call.parameters["id"].validateIsNull(call) ?: return@get

        val post = withContext(Dispatchers.IO) {
            db.collection("posts").document(postId).get().get()
        }.toObject(Post::class.java)


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
        val postId = call.parameters["id"].validateIsNull(call) ?: return@delete

        val user = getUser(call) ?: return@delete

        val post = withContext(Dispatchers.IO) {
            db.collection("posts").document(postId).get().get()
        }.toObject(Post::class.java)

        if (post == null) {
            call.respond(HttpStatusCode.NotFound, "Post not found")
            return@delete
        }

        if (user.id != post.author && user.role != Role.MOD.name && user.role != Role.ADMIN.name) {
            call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            return@delete
        }

        db.collection("posts").document(postId).delete()

        call.respond(HttpStatusCode.OK, "Post deleted")
    }
}
