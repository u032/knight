package wiki.chess.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.models.Post

object PostService {
    private const val collectionName = "posts"

    suspend fun getAllPosts(): List<Post> {
        val postsDocuments = withContext(Dispatchers.IO) {
            db.collection(collectionName).get().get().documents
        }

        val posts: ArrayList<Post> = ArrayList()

        postsDocuments.forEach { post ->
            posts.add(post.toObject(Post::class.java))
        }

        return posts
    }

    suspend fun getPostById(id: String): Post? {
        return withContext(Dispatchers.IO) {
            db.collection(collectionName).document(id).get().get()
        }.toObject(Post::class.java)
    }

    fun deletePost(post: Post): Boolean {
        db.collection(collectionName).document(post.id.toString()).delete()
        return true
    }
}
