package wiki.chess.models

@kotlinx.serialization.Serializable
data class Post(
    var id: String = "",
    val created: Long = 0,
    val edited: Long = 0,
    val date: Long = 0,
    val author: String = "",
    val content: String = "",
    val votes: Long = 0
)
