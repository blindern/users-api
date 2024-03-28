package no.foreningenbs.usersapi.ldap

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.ldap.Reference.UserRef

data class User(
  val id: Int,
  val username: String,
  val email: String?,
  val realname: String?,
  val phone: String?,
) {
  val reference by lazy { UserRef(username) }

  private fun toMutableMap(): MutableMap<String, Any?> =
    mutableMapOf(
      "id" to id,
      "unique_id" to username,
      "username" to username,
      "email" to email,
      "realname" to realname,
      "phone" to phone,
    )

  fun toResponse(
    dataProvider: DataProvider,
    withRelations: Boolean,
    withGroupsDetailed: Boolean,
  ): Map<String, Any?> {
    val map = toMutableMap()

    if (withRelations) {
      val data = dataProvider.getData()

      map["groups_relation"] = data.userGroups[reference]
        ?.map { (groupRef, groupRefList) ->
          groupRef.groupname to groupRefList.map { it.groupname }
        }
        ?.toMap()
        ?: emptyMap<Any, Any>()

      map["groupsowner_relation"] = data.userOwns[reference]
        ?.map { (groupRef, groupRefList) ->
          groupRef.groupname to groupRefList.map { it.groupname }
        }
        ?.toMap()
        ?: emptyMap<Any, Any>()
    }

    if (withGroupsDetailed) {
      val data = dataProvider.getData()

      map["groups"] = data.userGroups[reference]
        ?.map { (groupRef, _) -> data.groups[groupRef]!! }
        ?.map {
          it.toResponse(
            dataProvider,
            withMembers = false,
            withOwners = false,
            withMembersData = false,
          )
        }
        ?.toList()
        ?: emptyList<Any>()
    }

    return map
  }

  companion object {
    // LDAP Fields
    const val id = "uidNumber"
    const val username = "uid"
    const val email = "mail"
    const val realname = "cn"
    const val phone = "mobile"

    val ldapFieldList =
      listOf(
        id,
        username,
        email,
        realname,
        phone,
      )
  }
}
