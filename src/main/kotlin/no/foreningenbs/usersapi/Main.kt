package no.foreningenbs.usersapi

import mu.KotlinLogging
import no.foreningenbs.usersapi.api.InvalidateCache
import no.foreningenbs.usersapi.api.auth.SimpleAuth
import no.foreningenbs.usersapi.api.groups.AddUserToGroup
import no.foreningenbs.usersapi.api.groups.GetGroup
import no.foreningenbs.usersapi.api.groups.ListGroups
import no.foreningenbs.usersapi.api.groups.RemoveUserFromGroup
import no.foreningenbs.usersapi.api.users.CreateUser
import no.foreningenbs.usersapi.api.users.GetUser
import no.foreningenbs.usersapi.api.users.ListUsers
import no.foreningenbs.usersapi.api.users.ModifyUser
import no.foreningenbs.usersapi.health.HealthController
import no.foreningenbs.usersapi.hmac.Hmac
import no.foreningenbs.usersapi.ldap.Ldap
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

private val logger = KotlinLogging.logger {}

fun main() {
  val ldap = Ldap(Config)
  val dataProvider = DataProvider(Config, ldap)

  server(ldap, dataProvider).start()
}

val loggingFilter =
  Filter { next ->
    { req ->
      try {
        val res = next(req)
        logger.info("[${res.status}] ${req.method} ${req.uri} (agent: ${req.header("user-agent")})")
        res
      } catch (e: Throwable) {
        logger.error("Exception caught for: ${req.method} ${req.uri} (agent: ${req.header("user-agent")})", e)
        Response(INTERNAL_SERVER_ERROR).body("Request failed. See logs")
      }
    }
  }

fun server(
  ldap: Ldap,
  dataProvider: DataProvider,
) = ServerFilters.GZip()
  .then(loggingFilter)
  .then(app(ldap, dataProvider))
  .asServer(Jetty(8000))

fun app(
  ldap: Ldap,
  dataProvider: DataProvider,
): HttpHandler {
  val authFilter =
    AuthFilter(
      Hmac(Config.HMAC_TIMEOUT, Config.hmacKey),
      // Currently using hmac key as also directly API key. We might
      // want to revisit this later, e.g. giving each application
      // its own key.
      Config.hmacKey,
      Config.enforceAuth,
    ).filter

  val routes =
    routes(
      "/" bind GET to {
        Response(OK).body("https://github.com/blindern/users-api")
      },
      "/health" bind GET to HealthController().handler,
      authFilter
        .then(
          routes(
            "/invalidate-cache" bind POST to InvalidateCache(dataProvider).handler,
            WrappedResponse.filter.then(
              routes(
                "/users" bind GET to ListUsers(dataProvider).handler,
                "/user/{username}" bind GET to GetUser(dataProvider).handler,
                "/groups" bind GET to ListGroups(dataProvider).handler,
                "/group/{groupname}" bind GET to GetGroup(dataProvider).handler,
                "/simpleauth" bind POST to SimpleAuth(ldap, dataProvider).handler,
              ),
            ),
            "/v2" bind
              routes(
                "/users" bind GET to ListUsers(dataProvider).handler,
                "/users" bind POST to CreateUser(ldap, dataProvider).handler,
                "/users/{username}" bind GET to GetUser(dataProvider).handler,
                "/users/{username}/modify" bind POST to ModifyUser(ldap, dataProvider).handler,
                "/groups" bind GET to ListGroups(dataProvider).handler,
                "/groups/{groupname}" bind GET to GetGroup(dataProvider).handler,
                "/groups/{groupname}/members/users/{username}" bind PUT to AddUserToGroup(ldap, dataProvider).handler,
                "/groups/{groupname}/members/users/{username}" bind DELETE to RemoveUserFromGroup(ldap, dataProvider).handler,
                "/simpleauth" bind POST to SimpleAuth(ldap, dataProvider).handler,
              ),
          ),
        ),
    )

  return ServerFilters.CatchLensFailure {
    logger.debug("Lens failure: ${it.message}", it)
    Response(BAD_REQUEST).body(it.failures.joinToString("; "))
  }
    .then(routes)
}
