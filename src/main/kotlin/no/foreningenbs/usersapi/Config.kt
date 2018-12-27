package no.foreningenbs.usersapi

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import java.nio.file.Paths
import java.time.Duration
import java.time.temporal.ChronoUnit

object Config {
  private val config =
    ConfigurationProperties.systemProperties() overriding
      EnvironmentVariables() overriding
      ConfigurationProperties.fromOptionalFile(
        Paths.get("overrides.properties").toFile()
      ) overriding
      ConfigurationProperties.fromResource("defaults.properties")

  class Ldap {
    val server = "ldapmaster.vpn.foreningenbs.no"
    val groupDn = "ou=Groups,dc=foreningenbs,dc=no"
    val userDn = "ou=Users,dc=foreningenbs,dc=no"
    val bindDn = "uid=USERNAME,ou=Users,dc=foreningenbs,dc=no"
    val adminDn = "cn=admin,dc=foreningenbs,dc=no"
    val adminPassword = config[Key("users-api.ldap-admin-password", stringType)]

    // don't show these groups
    val groupsIgnore = listOf(
      "Domain Users",
      "Domain Admins",
      "Account Operators",
      "Administrators",
      "Backup Operators",
      "Domain Computers",
      "Domain Guests",
      "Print Operators",
      "Replicators"
    )
  }

  val ldap = Ldap()
  val cacheTimeout: Duration = Duration.of(300L, ChronoUnit.SECONDS)
  val hmacKey = config[Key("users-api.hmac-key", stringType)]
  const val hmacTimeout = 300L // allow 5 minutes time delay
}
