package no.foreningenbs.usersapi.api.groups

import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.ldap.Ldap

class RemoveOwnerFromGroup(
  ldap: Ldap,
  dataProvider: DataProvider,
) {
  val handler =
    groupMembershipHandler(ldap, dataProvider, "ownerType", "ownerId", Ldap::removeGroupOwner)
}
