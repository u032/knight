package wiki.chess.models

@kotlinx.serialization.Serializable
data class DiscordError(
    val error: String = "",
    val error_description: String = ""
)
