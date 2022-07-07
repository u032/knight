package wiki.chess.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.models.Post

object PostService {
    private const val collectionName = "posts"

    suspend fun getAllPosts(): MutableMap<String, Post> {
        val posts: MutableMap<String, Post> = mutableMapOf()

        withContext(Dispatchers.IO) {
            db.collection(collectionName).get().get().documents
        }.forEach { post ->
            posts[post.id] = post.toObject(Post::class.java)
        }

        return posts
    }

    suspend fun getPostById(id: String): Post? {
        return withContext(Dispatchers.IO) {
            db.collection(collectionName).document(id).get().get()
        }.toObject(Post::class.java)
    }

    fun incrementVotes(post: Post) {
        db.collection(collectionName).document(post.id.toString()).update("votes", post.votes + 1)
    }

    fun decrementVotes(post: Post) {
        db.collection(collectionName).document(post.id.toString()).update("votes", post.votes - 1)
    }

    fun deletePost(post: Post) {
        db.collection(collectionName).document(post.id.toString()).delete()
    }
}
