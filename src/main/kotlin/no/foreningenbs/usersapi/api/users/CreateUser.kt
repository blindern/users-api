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
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Moshi.auto
import org.http4k.lens.Header

class CreateUser(private val ldap: Ldap, private val dataProvider: DataProvider) {
  val bodyLens = Body.auto<Content>().toLens()

  val handler = handler@{ req: Request ->
    if (Header.CONTENT_TYPE(req)?.equalsIgnoringDirectives(ContentType.APPLICATION_JSON) != true) {
      return@handler Response(Status.BAD_REQUEST).body("Invalid content type")
    }

    val body = bodyLens(req)

    // We validate the username since it's pretty important that we
    // don't accept strange values there, but for the other fields
    // it is assumed that the caller performs all kinds of validation.
    if (!body.username.matches(Regex("^[a-z][a-z\\d]+$"))) {
      return@handler Response(Status.BAD_REQUEST).body("Invalid username")
    }

    // Don't use the cache for these operations to ensure we work
    // on the current data.

    // Two users cannot have either the same username nor the same email address.
    val existingUsername = ldap.getUsers("(uid=%s)".format(escape(body.username)))
    if (existingUsername.isNotEmpty()) {
      return@handler Response(Status.BAD_REQUEST).body("Username already in use")
    }

    val existingEmail = ldap.getUsers("(mail=%s)".format(escape(body.email)))
    if (existingEmail.isNotEmpty()) {
      return@handler Response(Status.BAD_REQUEST).body("Email address already in use")
    }

    // Okay - let's go ahead create it.

    ldap.createUser(
      username = body.username,
      firstName = body.firstName,
      lastName = body.lastName,
      email = body.email,
      phone = body.phone,
      passwordInPlaintext = body.passwordInPlaintext,
    )

    dataProvider.invalidateCache()

    val user =
      dataProvider.getData().users[Reference.UserRef(body.username)]
        ?: return@handler Response(Status.INTERNAL_SERVER_ERROR)

    Response(OK).with(
      jsonMapLens of
        user.toResponse(
          dataProvider,
          withRelations = true,
          withGroupsDetailed = true,
        ),
    )
  }

  data class Content(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val passwordInPlaintext: String?,
  )
}
