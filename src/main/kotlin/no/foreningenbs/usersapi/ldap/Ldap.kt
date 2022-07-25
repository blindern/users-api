package no.foreningenbs.usersapi.ldap

import no.foreningenbs.usersapi.Config
import no.foreningenbs.usersapi.ldap.Reference.GroupRef
import no.foreningenbs.usersapi.ldap.Reference.UserRef
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Enumeration
import java.util.Hashtable
import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.directory.Attribute
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes
import javax.naming.directory.DirContext
import javax.naming.directory.ModificationItem
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext
import javax.naming.ldap.StartTlsRequest
import javax.naming.ldap.StartTlsResponse

class Ldap(private val config: Config) {
  private val dnPattern = "^(.+?)=(.+?),(.+)$".toRegex().toPattern()

  fun testCredentials(username: String, password: String) =
    try {
      withConnection(userDn(username), password) { ctx ->
        // Perform a search so it will force a bind
        ctx.search(config.ldap.usersDn, "(uid=%s)".format(escape(username)), listOf(User.id))
      }
      true
    } catch (e: AuthenticationException) {
      false
    }

  fun userRdn(username: String) = "%s=%s".format(config.ldap.userRdnName, escape(username))
  fun userDn(username: String) = "%s,%s".format(userRdn(username), config.ldap.usersDn)
  fun groupRdn(groupname: String) = "%s=%s".format(config.ldap.groupRdnName, escape(groupname))
  fun groupDn(groupname: String) = "%s,%s".format(groupRdn(groupname), config.ldap.groupsDn)

  fun <R> withConnection(
    dn: String = config.ldap.adminDn,
    password: String = config.ldap.adminPassword,
    allowSlave: Boolean = true,
    block: (LdapContext) -> R,
  ): R {
    val server = when (allowSlave) {
      true -> config.ldap.server
      false -> config.ldap.masterServer
    }

    val env = Hashtable<String, Any>(11)
    env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
    env[Context.PROVIDER_URL] = "ldap://$server:389"

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
        .search(config.ldap.usersDn, filter, User.ldapFieldList)
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
        .search(config.ldap.groupsDn, fullFilter, Group.ldapFieldList)
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
        .associateBy {
          GroupRef(it.name)
        }
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
        config.ldap.usersDn -> acc + UserRef(match.group(2)!!)
        config.ldap.groupsDn -> acc + GroupRef(match.group(2)!!)
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
        .search(config.ldap.usersDn, "(objectClass=posixAccount)", listOf(User.id))
        .map { it.attributes[User.id].first().toInt() }
        .filter { it < 60_000 }
        .maxOrNull() ?: 9999

      max + 1
    }

  fun getNextGid() =
    withConnection { ctx ->
      val max = ctx
        .search(config.ldap.groupsDn, "(objectClass=posixGroup)", listOf(Group.id))
        .maxOfOrNull { it.attributes[Group.id].first().toInt() } ?: 0

      max + 1
    }

  fun generatePasswordHash(plaintext: String): String {
    val salt = ByteArray(4).also {
      SecureRandom().nextBytes(it)
    }

    val hash = MessageDigest.getInstance("SHA-1").let {
      it.update(plaintext.encodeToByteArray())
      it.update(salt)
      it.digest()
    }

    return "{SSHA}" + Base64.getEncoder().encodeToString(hash + salt)
  }

  fun createUser(
    username: String,
    firstName: String,
    lastName: String,
    email: String,
    phone: String?,
    passwordInPlaintext: String?,
  ) {
    withConnection(allowSlave = false) { ctx ->
      val attributes = BasicAttributes()

      // Some details about the Samba-specific fields can be seen here:
      // https://www.linuxtopia.org/online_books/network_administration_guides/samba_reference_guide/18_passdb_23.html

      // This is based on how users have been created in the past.

      val uidNumber = getNextUid()

      attributes.put("cn", "$firstName $lastName")
      attributes.put("displayName", "$firstName $lastName")
      attributes.put("mail", email)
      attributes.put("gecos", "System User")
      attributes.put("gidNumber", "513") // 513 == brukere
      attributes.put("givenName", firstName)
      attributes.put("homeDirectory", "/home/$username")
      attributes.put("loginShell", "/usr/sbin/nologin")
      if (phone != null) {
        attributes.put("mobile", phone)
      }

      val objectClass = BasicAttribute("objectClass")
      objectClass.add("top")
      objectClass.add("person")
      objectClass.add("organizationalPerson")
      objectClass.add("posixAccount")
      objectClass.add("shadowAccount")
      objectClass.add("inetOrgPerson")
      objectClass.add("sambaSamAccount")
      attributes.put(objectClass)

      if (passwordInPlaintext != null) {
        attributes.put("userPassword", generatePasswordHash(passwordInPlaintext))
      }
      attributes.put("sambaAcctFlags", "[UX]")
      attributes.put("sambaHomePath", "\\\\\\$username")
      attributes.put("sambaKickoffTime", "2147483647")
      // attributes.put("sambaLMPassword", "xxx")
      attributes.put("sambaLogoffTime", "2147483647")
      attributes.put("sambaLogonTime", "0")
      // attributes.put("sambaNTPassword", "xxx")
      attributes.put("sambaPrimaryGroupSID", "S-1-5-21-3661172500-3094412630-135700027-513")
      attributes.put("sambaProfilePath", "\\\\\\profiles\\$username")
      attributes.put("sambaPwdCanChange", "0")
      attributes.put("sambaPwdLastSet", "0")
      attributes.put("sambaPwdMustChange", "2147483647")
      attributes.put("sambaSID", "S-1-5-21-3661172500-3094412630-135700027-${uidNumber * 2 + 1000}")
      attributes.put("sn", lastName)
      attributes.put("uidNumber", "$uidNumber")

      ctx.createSubcontext(userDn(username), attributes)
    }
  }

  data class StringValue(val value: String)
  data class OptionalStringValue(val value: String?)

  /**
   * Modify a user.
   */
  fun modifyUser(
    username: String,
    firstName: StringValue?,
    lastName: StringValue?,
    email: StringValue?,
    phone: OptionalStringValue?,
    passwordInPlaintext: OptionalStringValue?,
  ) {
    data class ExistingData(
      val firstName: String,
      val lastName: String,
    )

    withConnection(allowSlave = false) { ctx ->
      val userDn = userDn(username)
      val existingAttr = ctx.getAttributes(
        userDn,
        arrayOf("givenName", "sn", "phone", "sambaLMPassword", "sambaNTPassword")
      )

      val attributes = mutableListOf<ModificationItem>()

      fun update(attribute: Attribute) {
        attributes.add(ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute))
      }

      fun remove(attributeName: String) {
        attributes.add(ModificationItem(DirContext.REMOVE_ATTRIBUTE, BasicAttribute(attributeName)))
      }

      if (firstName != null) {
        update(BasicAttribute("givenName", firstName.value))
      }

      if (lastName != null) {
        update(BasicAttribute("sn", lastName.value))
      }

      if (firstName != null || lastName != null) {
        val fullName = listOf(
          firstName?.value ?: existingAttr.get("givenName").first(),
          lastName?.value ?: existingAttr.get("sn").first(),
        ).joinToString(" ")

        update(BasicAttribute("cn", fullName))
        update(BasicAttribute("displayName", fullName))
      }

      if (email != null) {
        update(BasicAttribute("mail", email.value))
      }

      if (phone != null) {
        if (phone.value != null) {
          update(BasicAttribute("phone", phone.value))
        } else if (existingAttr.get("phone") != null) {
          remove("phone")
        }
      }

      if (passwordInPlaintext != null) {
        if (passwordInPlaintext.value != null) {
          update(BasicAttribute("userPassword", generatePasswordHash(passwordInPlaintext.value)))
        } else if (existingAttr.get("userPassword") != null) {
          remove("userPassword")
        }

        // Let's avoid the insecure SHA4 Samba password for now.
        // Shouldn't really be needed any more since ipps:// is usually used nowadays.
        if (existingAttr.get("sambaLMPassword") != null) {
          remove("sambaLMPassword")
        }
        if (existingAttr.get("sambaNTPassword") != null) {
          remove("sambaNTPassword")
        }
      }

      if (attributes.size > 0) {
        ctx.modifyAttributes(userDn, attributes.toTypedArray())
      }
    }
  }

  /**
   * Add member to a group.
   */
  fun addGroupMember(group: GroupRef, member: Reference) {
    withConnection(allowSlave = false) { ctx ->
      val existingAttr = ctx.getAttributes(
        groupDn(group.groupname),
        arrayOf(Group.members)
      )

      val memberValue = when (member) {
        is GroupRef -> groupDn(member.groupname)
        is UserRef -> userDn(member.username)
      }

      val membersAttribute = existingAttr[Group.members] ?: BasicAttribute(Group.members)

      if (memberValue !in membersAttribute) {
        membersAttribute.add(memberValue)

        val attributes = listOf(
          ModificationItem(DirContext.REPLACE_ATTRIBUTE, membersAttribute)
        )

        ctx.modifyAttributes(groupDn(group.groupname), attributes.toTypedArray())
      }
    }
  }

  /**
   * Remove member from a group.
   */
  fun removeGroupMember(group: GroupRef, member: Reference) {
    withConnection(allowSlave = false) { ctx ->
      val existingAttr = ctx.getAttributes(
        groupDn(group.groupname),
        arrayOf(Group.members)
      )

      val memberValue = when (member) {
        is GroupRef -> groupDn(member.groupname)
        is UserRef -> userDn(member.username)
      }

      val membersAttribute = existingAttr[Group.members] ?: BasicAttribute(Group.members)

      if (memberValue in membersAttribute) {
        membersAttribute.remove(memberValue)

        val attributes = listOf(
          ModificationItem(DirContext.REPLACE_ATTRIBUTE, membersAttribute)
        )

        ctx.modifyAttributes(groupDn(group.groupname), attributes.toTypedArray())
      }
    }
  }

  /**
   * Generate cache of all users and groups
   */
  fun getAllData(): AllData {
    val users = getUsers()
    val groups = getGroups()

    val memberExpander = MemberExpander(groups)
    val groupMembers: Map<GroupRef, Map<UserRef, List<GroupRef>>> = groups.values.associate { group ->
      val members = memberExpander.parse(group)
        .toList()
        .sortedBy { (userRef, _) ->
          users[userRef]?.realname ?: userRef.username
        }
        .toMap()

      group.reference to members
    }

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
          .getOrPut(email.lowercase()) { mutableListOf() }
          .add(UserRef(user.username))
      }
    }

    // Create reference from phone numbers to usernames.
    val phoneNumbers = mutableMapOf<String, MutableList<UserRef>>()
    users.values.forEach { user ->
      user.phone?.let { phone ->
        phoneNumbers
          .getOrPut(phone.lowercase()) { mutableListOf() }
          .add(UserRef(user.username))
      }
    }

    return AllData(users, groups, groupMembers, userGroups, userOwns, emails, phoneNumbers)
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
  val emails: Map<String, List<UserRef>>,
  val phoneNumbers: Map<String, List<UserRef>>
)

sealed class Reference {
  data class UserRef(val username: String) : Reference() {
    override fun toString(): String = "user:$username"
  }

  data class GroupRef(val groupname: String) : Reference() {
    override fun toString(): String = "group:$groupname"
  }
}
