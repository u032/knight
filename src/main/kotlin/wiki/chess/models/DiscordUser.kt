package wiki.chess.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DiscordUser(
    val id: String = "",
    val username: String = "",
    val avatar: String = "",
    @Transient val error: DiscordError? = null
)
