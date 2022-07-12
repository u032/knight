package wiki.chess.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.models.Post

object PostService {
    private const val collectionName = "posts"

    suspend fun getPosts(limit: Int, before: String): Map<String, Post> {
        return GeneralService.get(collectionName, limit, before) { post ->
            post.toObject(Post::class.java)
        }
    }

    suspend fun getPostById(id: String): Post? {
        val post = withContext(Dispatchers.IO) {
            db.collection(collectionName).document(id).get().get()
        }.toObject(Post::class.java)

        post?.id = id

        return post
    }

    fun incrementVotes(post: Post) {
        db.collection(collectionName).document(post.id).update("votes", post.votes + 1)
    }

    fun decrementVotes(post: Post) {
        db.collection(collectionName).document(post.id).update("votes", post.votes - 1)
    }

    fun deletePost(post: Post) {
        db.collection(collectionName).document(post.id).delete()
    }
}
