package wiki.chess.models

import kotlinx.serialization.Serializable
import wiki.chess.enums.*

@Serializable
data class User(
    var id: String = "0",
    val avatar: String = "",
    val name: String = "",
    val bio: String = "",
    val references: ArrayList<String> = ArrayList(),
    val country: Country? = null,
    var email: String = "",
    val federation: Federation? = null,
    val rating: Long = 1000,
    val role: Role = Role.USER,
    val sex: Sex? = null,
    val title: Title? = null,
    var notifications: Map<String, Map<String, String>> = mapOf(),
    val birthday: Long = 0,
    val registered_at: Long = 0
)
