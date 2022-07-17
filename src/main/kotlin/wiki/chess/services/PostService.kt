package wiki.chess.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db
import wiki.chess.models.Post
import wiki.chess.toPost

object PostService {
    private const val collectionName = "posts"

    /**
     * > This function gets a list of posts from the database, and returns them as a list of Post objects
     *
     * @param limit The number of posts to get
     * @param before The post ID of the post you want to start from.
     * @param sort The sort order of the posts. Can be "new", "top", "hot", "controversial", "old", "random", "qa", "live",
     * "votes"
     * @return A list of posts
     */
    suspend fun getPosts(limit: Int, before: String, sort: String): List<Post> {
        return GeneralService.get(collectionName, limit, before, sort) { post ->
            post.toPost()
        }
    }

    /**
     * > This function gets a post from the database by its id
     *
     * @param id The id of the post to get
     * @return A Post object
     */
    suspend fun getPostById(id: String): Post? {
        val post = withContext(Dispatchers.IO) {
            db.collection(collectionName).document(id).get().get()
        }.toObject(Post::class.java)

        post?.id = id

        return post
    }

    /**
     * Update the votes field of the document with the given id to be one more than it currently is.
     *
     * @param post Post - This is the post that we want to increment the votes for.
     */
    fun incrementVotes(post: Post) {
        db.collection(collectionName).document(post.id).update("votes", post.votes + 1)
    }

    /**
     * Decrement the votes of a post by 1.
     *
     * @param post Post - this is the post that we want to increment the votes for.
     */
    fun decrementVotes(post: Post) {
        db.collection(collectionName).document(post.id).update("votes", post.votes - 1)
    }

    /**
     * Update the document with the given id in the given collection with the given data.
     *
     * @param post Post - The post object that you want to edit
     * @param data Map<String, Any>
     */
    fun editPost(post: Post, data: Map<String, Any>) {
        db.collection(collectionName).document(post.id).update(data)
    }

    /**
     * It deletes a post from the database
     *
     * @param post Post - this is the post object that we want to delete.
     */
    fun deletePost(post: Post) {
        db.collection(collectionName).document(post.id).delete()
    }
}
