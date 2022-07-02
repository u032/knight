package wiki.chess.models

@kotlinx.serialization.Serializable
data class AccessToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val refresh_token: String,
    val scope: String
)
