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

@Serializable
@Resource("/oauth2")
class OAuth2 {
    @Serializable
    @Resource("token")
    class Token(val parent: OAuth2 = OAuth2())
}
