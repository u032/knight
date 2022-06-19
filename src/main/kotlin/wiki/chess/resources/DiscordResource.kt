package wiki.chess.resources

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class Users {
    @Serializable
    @Resource("@me")
    class Me(val parent: Users = Users())
}
