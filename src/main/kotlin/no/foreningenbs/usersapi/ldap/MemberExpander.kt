package no.foreningenbs.usersapi.ldap

import no.foreningenbs.usersapi.ldap.Reference.GroupRef
import no.foreningenbs.usersapi.ldap.Reference.UserRef

class MemberExpander(private val groups: Map<GroupRef, Group>) {
  private val parsed = mutableMapOf<GroupRef, Map<UserRef, MutableList<GroupRef>>>()

  /**
   * Expand the real members array to all references
   * Keep a reference to where the user is originating from
   *
   * @return Array of members of the current group, referencing the groups that
   * gave this association
   */
  fun parse(
    group: Group,
    visited: List<String> = emptyList(),
  ): Map<UserRef, List<GroupRef>> {
    if (group.dn in visited) {
      return emptyMap()
    }

    return parsed.getOrPut(group.reference) {
      val groupMap = mutableMapOf<UserRef, MutableList<GroupRef>>()

      group.members.forEach { member ->
        when (member) {
          is UserRef -> {
            groupMap
              .getOrPut(member) { mutableListOf() }
              .add(group.reference)
          }
          is GroupRef -> {
            groups[member]?.let { childGroup ->
              parse(childGroup, visited + group.dn)
                .keys
                .forEach { user ->
                  groupMap
                    .getOrPut(user) { mutableListOf() }
                    .add(childGroup.reference)
                }
            }
          }
        }
      }

      groupMap
    }
  }
}
