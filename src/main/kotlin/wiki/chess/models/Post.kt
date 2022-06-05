package wiki.chess.models

@kotlinx.serialization.Serializable
data class Post(
    val id: Long = 0,
    val date: Long = 0,
    val author: Long = 0,
    val content: String = ""
)
