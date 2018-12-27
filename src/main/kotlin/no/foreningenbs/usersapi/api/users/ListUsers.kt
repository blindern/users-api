package no.foreningenbs.usersapi.api.users

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.MoshiBody.jsonArrayMapLens
import no.foreningenbs.usersapi.ldap.Reference
import no.foreningenbs.usersapi.ldap.User
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.int

class ListUsers(private val dataProvider: DataProvider) {
  private val usernamesLens = Query.optional("usernames")
  private val emailsLens = Query.optional("emails")
  private val grouplevelLens = Query.int().defaulted("grouplevel", 0)

  val handler = handler@{ req: Request ->
    val usernames = usernamesLens(req)
    val emails = emailsLens(req)
    val data = dataProvider.getData()

    val users = when {
      usernames != null -> {
        val list = mutableMapOf<Reference.UserRef, User>()
        usernames.split(",").forEach { username ->
          val userRef = Reference.UserRef(username)
          data.users[userRef]?.let { user ->
            list[userRef] = user
          }
        }
        list.values
      }
      emails != null -> {
        val list = mutableMapOf<Reference.UserRef, User>()
        emails.split(",").forEach { email ->
          data.emails[email]?.forEach { userRef ->
            val user = data.users[userRef]
            check(user != null)
            list[userRef] = user
          }
        }
        list.values
      }
      else -> data.users.values
    }.toList()

    val grouplevel = grouplevelLens(req).let {
      if (it < 0 || it > 3) 0
      else it
    }

    Response(OK).with(
      jsonArrayMapLens of
        users.map {
          it.toResponse(dataProvider, grouplevel >= 1, grouplevel >= 2)
        }.toTypedArray()
    )
  }
}
