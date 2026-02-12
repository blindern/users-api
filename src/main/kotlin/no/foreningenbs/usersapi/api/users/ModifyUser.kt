package no.foreningenbs.usersapi.api.users

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.MoshiBody.jsonMapLens
import no.foreningenbs.usersapi.ldap.Ldap
import no.foreningenbs.usersapi.ldap.Reference
import no.foreningenbs.usersapi.ldap.escape
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Moshi.auto
import org.http4k.lens.Header
import org.http4k.lens.Path

class ModifyUser(
  private val ldap: Ldap,
  private val dataProvider: DataProvider,
) {
  val bodyLens = Body.auto<Content>().toLens()

  val handler = handler@{ req: Request ->
    val username = Path.of("username")(req)

    if (Header.CONTENT_TYPE(req)?.equalsIgnoringDirectives(ContentType.APPLICATION_JSON) != true) {
      return@handler Response(Status.BAD_REQUEST).body("Invalid content type")
    }

    dataProvider.getData().users[Reference.UserRef(username)]
      ?: return@handler Response(Status.NOT_FOUND)

    val body = bodyLens(req)

    if (body.email != null) {
      val usersWithEmail = ldap.getUsers(filter = "(mail=${escape(body.email.value)})").values.map { it.username }
      if (usersWithEmail.isNotEmpty() && usersWithEmail != listOf(username)) {
        return@handler Response(Status.BAD_REQUEST).body("Email used by another user")
      }
    }

    ldap.modifyUser(
      username = username,
      firstName = body.firstName?.toLdapValue(),
      lastName = body.lastName?.toLdapValue(),
      email = body.email?.toLdapValue(),
      phone = body.phone?.toLdapValue(),
      passwordInPlaintext = body.passwordInPlaintext?.toLdapValue(),
    )

    dataProvider.invalidateCache()

    val user =
      dataProvider.getData().users[Reference.UserRef(username)]
        ?: return@handler Response(INTERNAL_SERVER_ERROR)

    Response(OK).with(
      jsonMapLens of
        user.toResponse(
          dataProvider,
          withRelations = true,
          withGroupsDetailed = true,
        ),
    )
  }

  data class StringValue(
    val value: String,
  ) {
    fun toLdapValue() = Ldap.StringValue(value)
  }

  data class OptionalStringValue(
    val value: String?,
  ) {
    fun toLdapValue() = Ldap.OptionalStringValue(value)
  }

  data class Content(
    val firstName: StringValue?,
    val lastName: StringValue?,
    val email: StringValue?,
    val phone: OptionalStringValue?,
    val passwordInPlaintext: OptionalStringValue?,
  )
}
