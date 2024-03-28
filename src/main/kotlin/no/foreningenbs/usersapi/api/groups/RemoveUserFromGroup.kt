package no.foreningenbs.usersapi.api.groups

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.ldap.Ldap
import no.foreningenbs.usersapi.ldap.Reference
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.lens.Path

class RemoveUserFromGroup(private val ldap: Ldap, private val dataProvider: DataProvider) {
  val handler = handler@{ req: Request ->
    val groupname = Path.of("groupname")(req)

    val group =
      dataProvider.getData().groups[Reference.GroupRef(groupname)]
        ?: return@handler Response(Status.NOT_FOUND).body("Group not found")

    val username = Path.of("username")(req)

    val user =
      dataProvider.getData().users[Reference.UserRef(username)]
        ?: return@handler Response(Status.NOT_FOUND).body("User not found")

    if (user.reference in group.members) {
      ldap.removeGroupMember(group.reference, user.reference)
    }

    dataProvider.invalidateCache()
    Response(NO_CONTENT)
  }
}
