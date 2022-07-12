package wiki.chess.services

import com.google.cloud.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wiki.chess.db

object GeneralService {
    suspend fun <T> get(
        collectionName: String,
        limit: Int,
        before: String,
        action: (query: QueryDocumentSnapshot) -> T
    ): Map<String, T> {
        val query: List<QueryDocumentSnapshot> = if (before.isEmpty()) {
            withContext(Dispatchers.IO) {
                db.collection(collectionName).limit(limit).get().get().documents
            }
        } else {
            withContext(Dispatchers.IO) {
                val beforeUser = db.collection(collectionName).document(before).get().get()
                db.collection(collectionName).startAfter(beforeUser).limit(limit).get().get().documents
            }
        }

        val objects: MutableMap<String, T> = mutableMapOf()

        query.forEach { obj ->
            objects[obj.id] = action.invoke(obj)
        }

        return objects
    }
}
