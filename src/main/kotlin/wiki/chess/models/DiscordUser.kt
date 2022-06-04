package wiki.chess.models

@kotlinx.serialization.Serializable
data class DiscordUser(
    val id: String,
    val username: String
)
