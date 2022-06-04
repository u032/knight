package wiki.chess.models

@kotlinx.serialization.Serializable
data class User(
    val name: String = "",
    val bio: String = "",
    val chessLink: String = "",
    val country: String = "",
    var email: String = "",
    val federation: String = "",
    val rating: Long = 0,
    val role: String = "",
    val sex: String = "",
    val title: String = ""
)
