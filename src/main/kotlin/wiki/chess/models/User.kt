package wiki.chess.models

import wiki.chess.enums.*

@kotlinx.serialization.Serializable
data class User(
    val id: Long = 0,
    val name: String = "",
    val bio: String = "",
    val chessLink: String = "",
    val country: Country? = null,
    var email: String = "",
    val federation: Federation? = null,
    val rating: Long = 0,
    val role: Role = Role.USER,
    val sex: Sex? = null,
    val title: Title? = null
)
