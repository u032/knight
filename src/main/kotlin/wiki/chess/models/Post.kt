package wiki.chess.models

@kotlinx.serialization.Serializable
data class Post(
    var id: String = "0",
    val created: Long = 0,
    val edited: Long = 0,
    val date: Long = 0,
    val author: String = "0",
    val content: String = "",
    val votes: Long = 0
)
