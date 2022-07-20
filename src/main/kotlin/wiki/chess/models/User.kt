package wiki.chess.models

import wiki.chess.enums.*

@kotlinx.serialization.Serializable
data class User(
    val id: String = "",
    val bot: Boolean = false,
    val avatar: String = "",
    val name: String = "",
    val bio: String = "",
    val references: List<String> = listOf(),
    val country: Country? = null,
    var email: String = "",
    val federation: Federation? = null,
    val rating: Long = 1000,
    val role: Role = Role.USER,
    val sex: Sex? = null,
    val title: Title? = null,
    var notifications: List<Notification> = listOf(),
    val birthday: Long = 0,
    val registered_at: Long = 0,
    val badges: List<Badge> = listOf(),
    val fide_id: Long = 0
)
