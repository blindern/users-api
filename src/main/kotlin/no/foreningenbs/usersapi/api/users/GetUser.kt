package no.foreningenbs.usersapi.api.users

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.MoshiBody.jsonMapLens
import no.foreningenbs.usersapi.ldap.Reference
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path

class GetUser(private val dataProvider: DataProvider) {
  val handler = handler@{ req: Request ->
    val username = Path.of("username")(req)
    val user = dataProvider.getData().users[Reference.UserRef(username)]
      ?: return@handler Response(Status.NOT_FOUND)

    Response(OK).with(
      jsonMapLens of user.toResponse(
        dataProvider,
        withRelations = true,
        withGroupsDetailed = true
      )
    )
  }
}
