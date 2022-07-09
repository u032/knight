package wiki.chess.models

import kotlinx.serialization.Transient

@kotlinx.serialization.Serializable
data class Post(
    @Transient var id: String = "0",
    val date: Long = 0,
    var author: String = "0",
    val content: String = "",
    val votes: Long = 0
)
