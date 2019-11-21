package no.foreningenbs.usersapi.ldap

import java.util.Enumeration
import java.util.Hashtable
import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.directory.Attribute
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext
import javax.naming.ldap.StartTlsRequest
import javax.naming.ldap.StartTlsResponse
import no.foreningenbs.usersapi.Config
import no.foreningenbs.usersapi.ldap.Reference.GroupRef
import no.foreningenbs.usersapi.ldap.Reference.UserRef

class Ldap(private val config: Config) {
  private val dnPattern = "^(.+?)=(.+?),(.+)$".toRegex().toPattern()

  fun testCredentials(username: String, password: String) =
    try {
      withConnection(getBindDn(username), password) { ctx ->
        // Perform a search so it will force a bind
        ctx.search(config.ldap.userDn, "(uid=%s)".format(escape(username)), listOf(User.id))
      }
      true
    } catch (e: AuthenticationException) {
      false
    }

  fun getBindDn(username: String) =
    config.ldap.bindDn.replace("USERNAME", username)

  fun <R> withConnection(
    dn: String = config.ldap.adminDn,
    password: String = config.ldap.adminPassword,
    block: (LdapContext) -> R
  ): R {
    val env = Hashtable<String, Any>(11)
    env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
    env[Context.PROVIDER_URL] = "ldap://${config.ldap.server}:389"

    val ctx = InitialLdapContext(env, null)
    try {
      val tls = ctx.extendedOperation(StartTlsRequest()) as StartTlsResponse
      tls.setHostnameVerifier { _, _ -> true }
      tls.negotiate(CustomSSLSocketFactory())

      // Perform simple client authentication
      ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple")
      ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn)
      ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password)

      return block(ctx)
    } finally {
      ctx.close()
    }
  }

  fun getUsers(filter: String = "(uid=*)"): Map<UserRef, User> =
    withConnection { ctx ->
      ctx
        .search(config.ldap.userDn, filter, User.ldapFieldList)
        .map {
          /* TODO: There can be multiple values for each attribute, however
              we only pick the first. */
          User(
            it.attributes[User.id].first().toInt(),
            it.attributes[User.username].first(),
            it.attributes[User.email]?.first(),
            it.attributes[User.realname]?.first(),
            it.attributes[User.phone]?.first()
          )
        }
        .sortedBy { it.realname }
        .map {
          UserRef(it.username) to it
        }
        .toMap()
    }

  fun getGroups(filter: String? = null): Map<GroupRef, Group> {
    val objectClassFilter = "(objectClass=posixGroup)"
    val fullFilter =
      if (filter == null) objectClassFilter
      else "(&%s%s)".format(objectClassFilter, filter)

    return withConnection { ctx ->
      ctx
        .search(config.ldap.groupDn, fullFilter, Group.ldapFieldList)
        .filterNot {
          (it.attributes[Group.name]?.get() as String) in config.ldap.groupsIgnore
        }
        .map { res ->
          val members = res.attributes[Group.members]?.list() ?: emptyList()
          val owners = res.attributes[Group.owners]?.list() ?: emptyList()

          /* TODO: There can be multiple values for each attribute, however
              we only pick the first. */
          Group(
            res.nameInNamespace,
            res.attributes[Group.id].first().toInt(),
            res.attributes[Group.name].first(),
            res.attributes[Group.description]?.first(),
            members.toReference(),
            owners.toReference()
          )
        }
        .sortedBy { it.name }
        .map {
          GroupRef(it.name) to it
        }
        .toMap()
    }
  }

  private fun List<String>.toReference() =
    fold(emptyList<Reference>()) fold@{ acc, item ->
      val match = dnPattern.matcher(item)
      if (!match.find()) {
        // TODO: Unexpected - log this
        return@fold acc
      }

      when (match.group(3)) {
        config.ldap.userDn -> acc + UserRef(match.group(2)!!)
        config.ldap.groupDn -> acc + GroupRef(match.group(2)!!)
        else -> {
          // TODO: Unexpected - log this
          acc
        }
      }
    }

  private fun buildOrFilter(field: String, values: List<String>) =
    values
      .map {
        "(%s=%s)".format(
          field,
          escape(it)
        )
      }
      .let {
        require(values.isNotEmpty())
        "(|%s)".format(it.joinToString(""))
      }

  fun getUsersByNames(names: List<String>): Map<UserRef, User> =
    getUsers(buildOrFilter(User.username, names))

  fun getGroupsByNames(names: List<String>): Map<GroupRef, Group> =
    getGroups(buildOrFilter(Group.name, names))

  fun getNextUid() =
    withConnection { ctx ->
      val max = ctx
        .search(config.ldap.userDn, "(objectClass=posixAccount)", listOf(User.id))
        .map { it.attributes[User.id].first().toInt() }
        .filter { it < 60_000 }
        .max() ?: 0

      max + 1
    }

  fun getNextGid() =
    withConnection { ctx ->
      val max = ctx
        .search(config.ldap.groupDn, "(objectClass=posixGroup)", listOf(Group.id))
        .map { it.attributes[Group.id].first().toInt() }
        .max() ?: 0

      max + 1
    }

  /**
   * Generate cache of all users and groups
   */
  fun getAllData(): AllData {
    val users = getUsers()
    val groups = getGroups()

    val memberExpander = MemberExpander(groups)
    val groupMembers: Map<GroupRef, Map<UserRef, List<GroupRef>>> = groups.values
      .map { group ->
        val members = memberExpander.parse(group)
          .toList()
          .sortedBy { (userRef, _) ->
            users[userRef]?.realname ?: userRef.username
          }
          .toMap()

        group.reference to members
      }
      .toMap()

    // Make reference to groups from users.
    val userGroups = mutableMapOf<UserRef, MutableMap<GroupRef, List<GroupRef>>>()
    groupMembers.forEach { (groupRef, members) ->
      members.forEach { (userRef, memberships) ->
        userGroups.getOrPut(userRef) { mutableMapOf() }[groupRef] = memberships
      }
    }

    // Make reference to owned groups from users.
    val userOwns = mutableMapOf<UserRef, MutableMap<GroupRef, MutableList<GroupRef>>>()
    groups.values.forEach { group ->
      group.owners.forEach { owner ->
        when (owner) {
          is UserRef -> {
            userOwns
              .getOrPut(owner) { mutableMapOf() }
              .getOrPut(group.reference) { mutableListOf() }
              .add(group.reference)
          }
          is GroupRef -> {
            groupMembers[owner]?.let { members ->
              members.keys.forEach { member ->
                userOwns
                  .getOrPut(member) { mutableMapOf() }
                  .getOrPut(group.reference) { mutableListOf() }
                  .add(owner)
              }
            }
          }
        }
      }
    }

    // Create reference from email addresses to usernames.
    val emails = mutableMapOf<String, MutableList<UserRef>>()
    users.values.forEach { user ->
      user.email?.let { email ->
        emails
          .getOrPut(email.toLowerCase()) { mutableListOf() }
          .add(UserRef(user.username))
      }
    }

    return AllData(users, groups, groupMembers, userGroups, userOwns, emails)
  }
}

/**
 * Returns a string which has the chars *, (, ), \ & NUL escaped to LDAP compliant
 * syntax as per RFC 2254.
 * Thanks and credit to Iain Colledge for the research and function.
 * (from MediaWiki LdapAuthentication-extension)
 */
fun escape(value: String) = value
  .replace("\\", "\\5c")
  .replace("(", "\\28")
  .replace(")", "\\29")
  .replace("*", "\\2a")
  .replace(0x0.toString(), "\\00")

fun <E> Enumeration<E>.toList(): List<E> {
  val list = mutableListOf<E>()
  while (hasMoreElements()) {
    list.add(nextElement())
  }
  return list
}

fun Attribute.first(): String = get() as String
fun Attribute.list(): List<String> = all?.toList()?.map { it as String } ?: emptyList()

fun LdapContext.search(dn: String, filter: String, fields: List<String>): List<SearchResult> {
  val searchControls = SearchControls()
  searchControls.searchScope = SearchControls.ONELEVEL_SCOPE
  searchControls.returningAttributes = fields.toTypedArray()

  return search(dn, filter, searchControls).toList()
}

data class AllData(
  val users: Map<UserRef, User>,
  val groups: Map<GroupRef, Group>,
  val groupMembers: Map<GroupRef, Map<UserRef, List<GroupRef>>>,
  val userGroups: Map<UserRef, Map<GroupRef, List<GroupRef>>>,
  val userOwns: Map<UserRef, Map<GroupRef, List<GroupRef>>>,
  val emails: Map<String, List<UserRef>>
)

sealed class Reference {
  data class UserRef(val username: String) : Reference() {
    override fun toString(): String = "user:$username"
  }

  data class GroupRef(val groupname: String) : Reference() {
    override fun toString(): String = "group:$groupname"
  }
}
