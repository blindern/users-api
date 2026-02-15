package no.foreningenbs.usersapi.api.groups

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.ldap.Ldap
import no.foreningenbs.usersapi.ldap.Reference
import no.foreningenbs.usersapi.ldap.Reference.GroupRef
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.lens.Path

fun groupMembershipHandler(
  ldap: Ldap,
  dataProvider: DataProvider,
  typePath: String,
  idPath: String,
  ldapAction: Ldap.(GroupRef, Reference) -> Unit,
): HttpHandler =
  handler@{ req: Request ->
    val groupname = Path.of("groupname")(req)
    val group =
      dataProvider.getData().groups[GroupRef(groupname)]
        ?: return@handler Response(NOT_FOUND).body("Group not found")

    val type = Path.of(typePath)(req)
    val id = Path.of(idPath)(req)
    val data = dataProvider.getData()

    val ref =
      when (type) {
        "users" -> {
          data.users[Reference.UserRef(id)]?.reference
            ?: return@handler Response(NOT_FOUND).body("User not found")
        }
        "groups" -> {
          data.groups[GroupRef(id)]?.reference
            ?: return@handler Response(NOT_FOUND).body("Group not found")
        }
        else -> return@handler Response(BAD_REQUEST).body("$typePath must be 'users' or 'groups'")
      }

    ldap.ldapAction(group.reference, ref)
    dataProvider.invalidateCache()
    Response(NO_CONTENT)
  }
