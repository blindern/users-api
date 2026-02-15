package no.foreningenbs.usersapi.api.groups

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import no.foreningenbs.usersapi.Config
import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.app
import no.foreningenbs.usersapi.createLdapMock
import no.foreningenbs.usersapi.ldap.Reference.GroupRef
import no.foreningenbs.usersapi.ldap.Reference.UserRef
import no.foreningenbs.usersapi.withHmac
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status

class GroupMembershipSpec :
  DescribeSpec({
    val ldap = createLdapMock()
    val dataProvider = DataProvider(Config, ldap)
    val app = app(ldap, dataProvider)

    every { ldap.addGroupMember(any(), any()) } returns Unit
    every { ldap.removeGroupMember(any(), any()) } returns Unit
    every { ldap.addGroupOwner(any(), any()) } returns Unit
    every { ldap.removeGroupOwner(any(), any()) } returns Unit

    describe("member endpoints") {
      it("PUT add user as member returns 204") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/members/users/halvargimnes").withHmac())
        res.status shouldBe Status.NO_CONTENT
        verify { ldap.addGroupMember(GroupRef("beboer"), UserRef("halvargimnes")) }
      }

      it("DELETE remove user as member returns 204") {
        val res = app(Request(Method.DELETE, "/v2/groups/beboer/members/users/halvargimnes").withHmac())
        res.status shouldBe Status.NO_CONTENT
        verify { ldap.removeGroupMember(GroupRef("beboer"), UserRef("halvargimnes")) }
      }

      it("PUT add group as member returns 204") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/members/groups/admin").withHmac())
        res.status shouldBe Status.NO_CONTENT
        verify { ldap.addGroupMember(GroupRef("beboer"), GroupRef("admin")) }
      }

      it("PUT nonexistent user returns 404") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/members/users/nonexistent").withHmac())
        res.status shouldBe Status.NOT_FOUND
        res.bodyString() shouldBe "User not found"
      }

      it("PUT nonexistent member group returns 404") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/members/groups/nonexistent").withHmac())
        res.status shouldBe Status.NOT_FOUND
        res.bodyString() shouldBe "Group not found"
      }

      it("PUT nonexistent target group returns 404") {
        val res = app(Request(Method.PUT, "/v2/groups/nonexistent/members/users/halvargimnes").withHmac())
        res.status shouldBe Status.NOT_FOUND
        res.bodyString() shouldBe "Group not found"
      }

      it("PUT invalid member type returns 400") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/members/invalid/foo").withHmac())
        res.status shouldBe Status.BAD_REQUEST
        res.bodyString() shouldBe "memberType must be 'users' or 'groups'"
      }
    }

    describe("owner endpoints") {
      it("PUT add user as owner returns 204") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/owners/users/halvargimnes").withHmac())
        res.status shouldBe Status.NO_CONTENT
        verify { ldap.addGroupOwner(GroupRef("beboer"), UserRef("halvargimnes")) }
      }

      it("DELETE remove user as owner returns 204") {
        val res = app(Request(Method.DELETE, "/v2/groups/beboer/owners/users/halvargimnes").withHmac())
        res.status shouldBe Status.NO_CONTENT
        verify { ldap.removeGroupOwner(GroupRef("beboer"), UserRef("halvargimnes")) }
      }

      it("PUT add group as owner returns 204") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/owners/groups/admin").withHmac())
        res.status shouldBe Status.NO_CONTENT
        verify { ldap.addGroupOwner(GroupRef("beboer"), GroupRef("admin")) }
      }
    }

    describe("auth") {
      it("request without HMAC returns 401") {
        val res = app(Request(Method.PUT, "/v2/groups/beboer/members/users/halvargimnes"))
        res.status shouldBe Status.UNAUTHORIZED
      }
    }
  })
