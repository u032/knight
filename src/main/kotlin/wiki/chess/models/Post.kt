package wiki.chess.models

@kotlinx.serialization.Serializable
data class Post(
    val author: String = " ",
    val content: String = " ",
    val date: Long = 0,
    val id: Long = 0
)
