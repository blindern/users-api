package no.foreningenbs.usersapi.api.auth

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.MoshiBody.jsonMapLens
import no.foreningenbs.usersapi.ldap.Ldap
import no.foreningenbs.usersapi.ldap.Reference
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.APPLICATION_FORM_URLENCODED
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Moshi.auto
import org.http4k.lens.FormField
import org.http4k.lens.Header
import org.http4k.lens.Validator
import org.http4k.lens.webForm

class SimpleAuth(private val ldap: Ldap, private val dataProvider: DataProvider) {
  private val usernameField = FormField.required("username")
  private val passwordField = FormField.required("password")
  private val bodyLens = Body.auto<Content>().toLens()

  val handler = handler@{ req: Request ->
    val body = if (Header.CONTENT_TYPE(req) == APPLICATION_FORM_URLENCODED) {
      val strictFormBody = Body.webForm(Validator.Strict, usernameField, passwordField).toLens()
      val validForm = strictFormBody.extract(req)
      Content(
        usernameField.extract(validForm),
        passwordField.extract(validForm)
      )
    } else {
      bodyLens(req)
    }

    if (body.username.isEmpty() || body.password.isEmpty()) {
      return@handler Response(BAD_REQUEST).body("Missing data")
    }

    if (ldap.testCredentials(body.username, body.password)) {
      val user = dataProvider.getData().users[Reference.UserRef(body.username)]!!.toResponse(
        dataProvider,
        withRelations = true,
        withGroupsDetailed = true
      )
      return@handler Response(OK).with(jsonMapLens of user)
    }

    Response(Status.UNAUTHORIZED).body("Invalid username/password")
  }

  data class Content(
    val username: String,
    val password: String
  )
}
