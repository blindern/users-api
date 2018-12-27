package no.foreningenbs.usersapi

import com.karumi.kotlinsnapshot.KotlinSnapshot
import io.mockk.every
import io.mockk.spyk
import no.foreningenbs.usersapi.hmac.Hmac
import no.foreningenbs.usersapi.ldap.Group
import no.foreningenbs.usersapi.ldap.Ldap
import no.foreningenbs.usersapi.ldap.Reference.GroupRef
import no.foreningenbs.usersapi.ldap.User
import org.http4k.core.Request

const val MOCK_AUTH_VALID_USERNAME = "halvargimnes"
const val MOCK_AUTH_VALID_PASSWORD = "password123"

val mockUser1 = User(
  1234,
  "halvargimnes",
  "halvargimnes@foreningenbs.no",
  "Halvar Gimnes",
  null
)

val mockUsers = mapOf(
  mockUser1.reference to mockUser1
)

val mockGroups =
  listOf(
    Group(
      "cn=beboer,ou=Groups,dc=foreningenbs,dc=no",
      1234,
      "beboer",
      null,
      listOf(
        mockUser1.reference
      ),
      listOf()
    )
  ).map { GroupRef(it.name) to it }.toMap()

fun createLdapMock(): Ldap =
  spyk(Ldap(Config)).also {
    every { it.testCredentials(any(), any()) } returns false
    every { it.testCredentials(MOCK_AUTH_VALID_USERNAME, MOCK_AUTH_VALID_PASSWORD) } returns true
    every { it.getGroups(any()) } returns mockGroups
    every { it.getUsers(any()) } returns mockUsers
  }

val hmac = Hmac(Config.hmacTimeout, Config.hmacKey)

fun Request.withHmac() = hmac.withHmac(this)

val kotlinSnapshot = KotlinSnapshot(snapshotsFolder = "src/test/resources")

infix fun Any.matchWithSnapshot(snapshotName: String) {
  kotlinSnapshot.matchWithSnapshot(this, snapshotName)
}
