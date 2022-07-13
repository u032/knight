package wiki.chess.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.models.Post
import wiki.chess.toPost

object PostService {
    private const val collectionName = "posts"

    suspend fun getPosts(limit: Int, before: String): List<Post> {
        return GeneralService.get(collectionName, limit, before, "", false) { post ->
            post.toPost()
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

    fun editPost(post: Post, data: Map<String, Any>) {
        db.collection(collectionName).document(post.id).update(data)
    }

    fun deletePost(post: Post) {
        db.collection(collectionName).document(post.id).delete()
    }
}
