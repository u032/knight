package wiki.chess.models

@kotlinx.serialization.Serializable
data class Post(
    val id: Long = 0,
    val date: Long = 0,
    var author: Long = 0,
    val content: String = "",
    val votes: Long = 0
)
