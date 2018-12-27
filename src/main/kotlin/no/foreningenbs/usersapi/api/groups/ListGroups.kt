package no.foreningenbs.usersapi.api.groups

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.MoshiBody.jsonArrayMapLens
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Query

class ListGroups(private val dataProvider: DataProvider) {
  private val groupnamesLens = Query.optional("groupnames")

  val handler = handler@{ req: Request ->
    val groupnames = groupnamesLens(req)
    val data = dataProvider.getData()

    val groups = data.groups.values
      .let { list ->
        if (groupnames == null) list
        else {
          val names = groupnames.split(",")
          list.filter {
            it.name in names
          }
        }
      }
      .toList()

    Response(OK).with(
      jsonArrayMapLens of
        groups.map {
          it.toResponse(
            dataProvider,
            withMembers = false,
            withOwners = false,
            withMembersData = false
          )
        }.toTypedArray()
    )
  }
}
