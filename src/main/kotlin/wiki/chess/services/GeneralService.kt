package wiki.chess.services

import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db

object GeneralService {
    /** A function that takes in a collection name, a limit, a before, a sort, and an action. It returns a list of T. */
    suspend fun <T> get(
        collectionName: String,
        limit: Int,
        before: String,
        sort: String,
        action: (query: QueryDocumentSnapshot) -> T
    ): List<T> {
        var collection: Query = db.collection(collectionName)

        if (sort.isNotEmpty()) {
            collection = collection.orderBy(sort)
        }

        val query: List<QueryDocumentSnapshot> = if (before.isEmpty()) {
            withContext(Dispatchers.IO) {
                collection.limit(limit).get().get().documents
            }
        } else {
            withContext(Dispatchers.IO) {
                val beforeUser = db.collection(collectionName).document(before).get().get()
                collection.startAfter(beforeUser).limit(limit).get().get().documents
            }
        }

        val objects: ArrayList<T> = ArrayList()

        query.forEach { obj ->
            objects.add(action.invoke(obj))
        }

        return objects
    }
}
