package no.foreningenbs.usersapi.api.groups

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.MoshiBody.jsonMapLens
import no.foreningenbs.usersapi.ldap.Reference
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path

class GetGroup(private val dataProvider: DataProvider) {
  val handler = handler@{ req: Request ->
    val groupname = Path.of("groupname")(req)
    val group = dataProvider.getData().groups[Reference.GroupRef(groupname)]
      ?: return@handler Response(Status.NOT_FOUND)

    Response(OK).with(
      jsonMapLens of group.toResponse(
        dataProvider,
        withMembers = true,
        withOwners = true,
        withMembersData = true
      )
    )
  }
}
