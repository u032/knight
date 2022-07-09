package wiki.chess.models

import kotlinx.serialization.Transient
import wiki.chess.enums.*

@kotlinx.serialization.Serializable
data class User(
    @Transient var id: String = "0",
    val name: String = "",
    val bio: String = "",
    val references: ArrayList<String> = ArrayList(),
    val country: Country? = null,
    var email: String = "",
    val federation: Federation? = null,
    val rating: Long = 0,
    val role: Role = Role.USER,
    val sex: Sex? = null,
    val title: Title? = null,
    var notifications: Map<String, Map<String, String>>? = null
)
