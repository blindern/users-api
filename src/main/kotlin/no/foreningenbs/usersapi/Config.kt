package no.foreningenbs.usersapi

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
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
        Paths.get("overrides.properties").toFile(),
      ) overriding
      ConfigurationProperties.fromResource("defaults.properties")

  class Ldap {
    val server = "ldap.zt.foreningenbs.no"
    val masterServer = "ldap-master.zt.foreningenbs.no"
    val groupsDn = "ou=Groups,dc=foreningenbs,dc=no"
    val groupRdnName = "cn"
    val usersDn = "ou=Users,dc=foreningenbs,dc=no"
    val userRdnName = "uid"
    val adminDn = "cn=admin,dc=foreningenbs,dc=no"
    val adminPassword = config[Key("users-api.ldap-admin-password", stringType)]

    // don't show these groups
    val groupsIgnore =
      listOf(
        "Domain Users",
        "Domain Admins",
        "Account Operators",
        "Administrators",
        "Backup Operators",
        "Domain Computers",
        "Domain Guests",
        "Print Operators",
        "Replicators",
      )
  }

  val ldap = Ldap()
  val cacheTimeout: Duration = Duration.of(300L, ChronoUnit.SECONDS)
  val hmacKey = config[Key("users-api.hmac-key", stringType)]
  const val HMAC_TIMEOUT = 300L // allow 5 minutes time delay

  // If set to false, all unrestricted access will be granted.
  // Only set false if needed during local development/testing.
  val enforceAuth = config[Key("users-api.enforce-auth", booleanType)]
}
