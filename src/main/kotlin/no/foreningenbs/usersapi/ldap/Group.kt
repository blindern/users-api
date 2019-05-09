package no.foreningenbs.usersapi.ldap

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.ldap.Reference.GroupRef
import no.foreningenbs.usersapi.ldap.Reference.UserRef

data class Group(
  val dn: String,
  val id: Int,
  val name: String,
  val description: String?,
  val members: List<Reference>,
  val owners: List<Reference>
) {
  val reference by lazy { GroupRef(name) }

  private fun toMutableMap(): MutableMap<String, Any?> =
    mutableMapOf(
      "id" to id,
      "unique_id" to name,
      "name" to name,
      "description" to description
    )

  private fun <R> List<Reference>.foldToType(mapper: (Reference) -> R) =
    fold(
      mutableMapOf(
        "users" to mutableListOf<R>(),
        "groups" to mutableListOf()
      )
    ) { acc, ref ->
      val type = when (ref) {
        is UserRef -> "users"
        is GroupRef -> "groups"
      }
      acc[type]!!.add(mapper(ref))
      acc
    }

  fun toResponse(
    dataProvider: DataProvider,
    withMembers: Boolean,
    withOwners: Boolean,
    withMembersData: Boolean
  ): Map<String, Any?> {
    val map = toMutableMap()

    if (withMembers) {
      map["members"] = members.foldToType {
        when (it) {
          is UserRef -> it.username
          is GroupRef -> it.groupname
        }
      }

      map["members_relation"] = dataProvider.getData().groupMembers[reference]
        ?.map { (userRef, groupRefList) ->
          userRef.username to groupRefList.map { it.groupname }
        }
        ?.toMap()
        ?: emptyMap<String, Any>()
    }

    if (withOwners) {
      map["owners"] = owners.foldToType {
        when (it) {
          is UserRef -> it.username
          is GroupRef -> it.groupname
        }
      }
    }

    if (withMembersData) {
      val data = dataProvider.getData()
      map["members_data"] = data.groupMembers[reference]
        ?.keys
        ?.mapNotNull {
          // Some user references point to invalid users. Typically because the
          // user has been changed or removed after the reference was added.
          data.users[it]?.let { user ->
            it.username to user.toResponse(
              dataProvider,
              withRelations = true,
              withGroupsDetailed = false
            )
          }
        }
        ?.toMap()
        ?: emptyMap<String, Any>()
    }

    return map
  }

  companion object {
    // LDAP Fields
    const val id = "gidNumber"
    const val name = "cn"
    const val members = "member" // memberUid
    const val description = "description"
    const val owners = "owner"

    val ldapFieldList = listOf(
      id,
      name,
      members,
      description,
      owners
    )
  }
}
