package no.foreningenbs.usersapi

import no.foreningenbs.usersapi.hmac.Hmac
import org.amshove.kluent.shouldEqual
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.with
import org.http4k.lens.Header.Common.CONTENT_TYPE
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

object MainSpec : Spek({
  describe("Main") {
    describe("app") {
      val ldap = createLdapMock()
      val dataProvider = DataProvider(Config, ldap)
      val app = app(ldap, dataProvider)

      describe("hmac protected route") {
        describe("request with missing hmac") {
          val res by memoized(mode = CachingMode.GROUP) {
            app(Request(Method.GET, "/users"))
          }

          it("should fail") {
            res.status shouldEqual Status.BAD_REQUEST
          }
        }

        describe("request with bad hmac") {
          val res by memoized(mode = CachingMode.GROUP) {
            val hmac = Hmac(100, "some invalid key")
            fun Request.withHmac() = hmac.withHmac(this)
            app(Request(Method.GET, "/users").withHmac())
          }

          it("should fail") {
            res.status shouldEqual Status.UNAUTHORIZED
          }
        }
      }

      data class Row(
        val title: String,
        val requestBuilder: () -> Request,
        val snapshotName: String,
        val status: Status = Status.OK
      )

      listOf(
        Row(
          "GET /groups",
          { Request(Method.GET, "/groups") },
          "GET_groups_body"
        ),
        Row(
          "GET /group/beboer",
          { Request(Method.GET, "/group/beboer") },
          "GET_group_beboer_body"
        ),
        Row(
          "GET /groups",
          { Request(Method.GET, "/groups") },
          "GET_groups_body"
        ),
        Row(
          "GET /group/beboer",
          { Request(Method.GET, "/group/beboer") },
          "GET_group_beboer_body"
        ),
        Row(
          "GET /user/unknown",
          { Request(Method.GET, "/user/unknown") },
          "GET_user_unknown_body",
          Status.NOT_FOUND
        ),
        Row(
          "GET /user/halvargimnes",
          { Request(Method.GET, "/user/halvargimnes") },
          "GET_user_halvargimnes_body"
        ),
        Row(
          "GET /user/halvargimnes?grouplevel=2",
          { Request(Method.GET, "/user/halvargimnes?grouplevel=2") },
          "GET_user_halvargimnes_grouplevel_2_body"
        ),
        Row(
          "GET /users",
          { Request(Method.GET, "/users") },
          "GET_users_body"
        ),
        Row(
          "GET /users?grouplevel=1",
          { Request(Method.GET, "/users?grouplevel=1") },
          "GET_users_grouplevel_1_body"
        ),
        Row(
          "GET /users?emails=example%40example.com",
          { Request(Method.GET, "/users?emails=example%40example.com") },
          "GET_users_email_no_match_body"
        ),
        Row(
          "GET /users?emails=halvargimnes%40foreningenbs.no",
          { Request(Method.GET, "/users?emails=halvargimnes%40foreningenbs.no") },
          "GET_users_email_with_match_body"
        )
      ).map { row ->
        describe(row.title) {
          val res by memoized(mode = CachingMode.GROUP) {
            app(row.requestBuilder().withHmac())
          }

          it("should return expected status") {
            res.status shouldEqual row.status
          }

          it("should return expected body") {
            res.bodyString() matchWithSnapshot row.snapshotName
          }
        }
      }

      describe("POST /simpleauth") {
        describe("using invalid credentials") {
          val res by memoized(mode = CachingMode.GROUP) {
            val req = Request(Method.POST, "/simpleauth")
              .with(CONTENT_TYPE of ContentType.APPLICATION_FORM_URLENCODED)
              .form("username", "somethingInvalid")
              .form("password", "test1234")
              .withHmac()
            app(req)
          }

          it("should fail") {
            res.status shouldEqual Status.UNAUTHORIZED
          }
        }

        describe("using valid credentials") {
          val res by memoized(mode = CachingMode.GROUP) {
            val req = Request(Method.POST, "/simpleauth")
              .with(CONTENT_TYPE of ContentType.APPLICATION_FORM_URLENCODED)
              .form("username", MOCK_AUTH_VALID_USERNAME)
              .form("password", MOCK_AUTH_VALID_PASSWORD)
              .withHmac()
            app(req)
          }

          it("should be successful") {
            res.status shouldEqual Status.OK
          }

          it("should return expected data") {
            res.bodyString() matchWithSnapshot "POST_simpleauth_body"
          }
        }
      }
    }
  }
})
