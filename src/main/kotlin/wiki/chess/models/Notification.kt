package wiki.chess.models

@kotlinx.serialization.Serializable
data class Notification(
    val id: String = "",
    val type: String = "unknown",
    val value: String = ""
)
