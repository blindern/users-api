package no.foreningenbs.usersapi

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import no.foreningenbs.usersapi.hmac.Hmac
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.with
import org.http4k.lens.Header.CONTENT_TYPE

class MainSpec :
  DescribeSpec({
    describe("Main") {
      describe("app") {
        val ldap = createLdapMock()
        val dataProvider = DataProvider(Config, ldap)
        val app = app(ldap, dataProvider)

        describe("hmac protected route") {
          describe("request with missing hmac") {
            it("should fail") {
              val res = app(Request(Method.GET, "/users"))
              res.status shouldBe Status.UNAUTHORIZED
            }
          }

          describe("request with bad hmac") {
            it("should fail") {
              val hmac = Hmac(100, "some invalid key")

              fun Request.withHmac() = hmac.withHmac(this)
              val res = app(Request(Method.GET, "/users").withHmac())
              res.status shouldBe Status.UNAUTHORIZED
            }
          }
        }

        data class Row(
          val title: String,
          val requestBuilder: () -> Request,
          val snapshotName: String,
          val status: Status = Status.OK,
        )

        listOf(
          Row(
            "GET /groups",
            { Request(Method.GET, "/groups") },
            "GET_groups_body",
          ),
          Row(
            "GET /group/beboer",
            { Request(Method.GET, "/group/beboer") },
            "GET_group_beboer_body",
          ),
          Row(
            "GET /user/unknown",
            { Request(Method.GET, "/user/unknown") },
            "GET_user_unknown_body",
            Status.NOT_FOUND,
          ),
          Row(
            "GET /user/halvargimnes",
            { Request(Method.GET, "/user/halvargimnes") },
            "GET_user_halvargimnes_body",
          ),
          Row(
            "GET /user/halvargimnes?grouplevel=2",
            { Request(Method.GET, "/user/halvargimnes?grouplevel=2") },
            "GET_user_halvargimnes_grouplevel_2_body",
          ),
          Row(
            "GET /users",
            { Request(Method.GET, "/users") },
            "GET_users_body",
          ),
          Row(
            "GET /users?grouplevel=1",
            { Request(Method.GET, "/users?grouplevel=1") },
            "GET_users_grouplevel_1_body",
          ),
          Row(
            "GET /users?emails=example%40example.com",
            { Request(Method.GET, "/users?emails=example%40example.com") },
            "GET_users_email_no_match_body",
          ),
          Row(
            "GET /users?emails=halvargimnes%40foreningenbs.no",
            { Request(Method.GET, "/users?emails=halvargimnes%40foreningenbs.no") },
            "GET_users_email_with_match_body",
          ),
        ).map { row ->
          describe(row.title) {
            val res = app(row.requestBuilder().withHmac())

            it("should return expected status") {
              res.status shouldBe row.status
            }

            it("should return expected body") {
              res.bodyString() matchWithSnapshot row.snapshotName
            }
          }
        }

        describe("POST /simpleauth using urlencoded form") {
          describe("using invalid credentials") {
            it("should fail") {
              val res =
                app(
                  Request(Method.POST, "/simpleauth")
                    .with(CONTENT_TYPE of ContentType.APPLICATION_FORM_URLENCODED)
                    .form("username", "somethingInvalid")
                    .form("password", "test1234")
                    .withHmac(),
                )
              res.status shouldBe Status.UNAUTHORIZED
            }
          }

          describe("using valid credentials") {
            val res =
              app(
                Request(Method.POST, "/simpleauth")
                  .with(CONTENT_TYPE of ContentType.APPLICATION_FORM_URLENCODED)
                  .form("username", MOCK_AUTH_VALID_USERNAME)
                  .form("password", MOCK_AUTH_VALID_PASSWORD)
                  .withHmac(),
              )

            it("should be successful") {
              res.status shouldBe Status.OK
            }

            it("should return expected data") {
              res.bodyString() matchWithSnapshot "POST_simpleauth_body"
            }
          }
        }

        describe("POST /simpleauth using JSON body") {
          describe("using valid credentials") {
            val body =
              """{"username":"$MOCK_AUTH_VALID_USERNAME","password":"$MOCK_AUTH_VALID_PASSWORD"}"""

            val res =
              app(
                Request(Method.POST, "/simpleauth")
                  .with(CONTENT_TYPE of ContentType.APPLICATION_JSON)
                  .body(body)
                  .withHmac(),
              )

            it("should be successful") {
              res.status shouldBe Status.OK
            }

            it("should return expected data") {
              res.bodyString() matchWithSnapshot "POST_simpleauth_body"
            }
          }
        }
      }
    }
  })
