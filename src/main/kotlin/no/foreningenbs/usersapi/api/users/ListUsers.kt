package no.foreningenbs.usersapi.api.users

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.MoshiBody.jsonArrayMapLens
import no.foreningenbs.usersapi.ldap.Reference
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.int

class ListUsers(private val dataProvider: DataProvider) {
  private val usernamesLens = Query.optional("usernames")
  private val emailsLens = Query.optional("emails")
  private val phoneNumbersLens = Query.optional("phoneNumbers")
  private val grouplevelLens = Query.int().defaulted("grouplevel", 0)

  val handler = handler@{ req: Request ->
    val usernames = usernamesLens(req)
    val emails = emailsLens(req)
    val phoneNumbers = phoneNumbersLens(req)
    val data = dataProvider.getData()

    val userRefs = mutableSetOf<Reference.UserRef>()
    var haveFilter = false

    if (usernames != null) {
      haveFilter = true
      userRefs.addAll(
        usernames.split(",").map(Reference::UserRef),
      )
    }

    if (emails != null) {
      haveFilter = true
      userRefs.addAll(
        emails.split(",").mapNotNull { email ->
          data.emails[email]
        }.flatten(),
      )
    }

    if (phoneNumbers != null) {
      haveFilter = true
      userRefs.addAll(
        phoneNumbers.split(",").mapNotNull { email ->
          data.phoneNumbers[email]
        }.flatten(),
      )
    }

    val users =
      if (haveFilter) {
        userRefs.mapNotNull { data.users[it] }
      } else {
        data.users.values
      }

    val grouplevel =
      grouplevelLens(req).let {
        if (it < 0 || it > 3) {
          0
        } else {
          it
        }
      }

    Response(OK).with(
      jsonArrayMapLens of
        users.map {
          it.toResponse(dataProvider, grouplevel >= 1, grouplevel >= 2)
        }.toTypedArray(),
    )
  }
}
