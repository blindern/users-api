package no.foreningenbs.usersapi

import mu.KotlinLogging
import no.foreningenbs.usersapi.api.InvalidateCache
import no.foreningenbs.usersapi.api.auth.SimpleAuth
import no.foreningenbs.usersapi.api.groups.GetGroup
import no.foreningenbs.usersapi.api.groups.ListGroups
import no.foreningenbs.usersapi.api.users.GetUser
import no.foreningenbs.usersapi.api.users.ListUsers
import no.foreningenbs.usersapi.health.HealthController
import no.foreningenbs.usersapi.hmac.Hmac
import no.foreningenbs.usersapi.hmac.HmacFilter
import no.foreningenbs.usersapi.ldap.Ldap
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_IMPLEMENTED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
  val ldap = Ldap(Config)
  val dataProvider = DataProvider(Config, ldap)

  server(ldap, dataProvider).start()
}

val loggingFilter = Filter { next ->
  { req ->
    val res = next(req)
    logger.info("[${res.status}] ${req.method} ${req.uri} (agent: ${req.header("user-agent")})")
    res
  }
}

fun server(ldap: Ldap, dataProvider: DataProvider) =
  ServerFilters.GZip()
    .then(ServerFilters.CatchAll())
    .then(loggingFilter)
    .then(app(ldap, dataProvider))
    .asServer(Jetty(8000))

fun app(ldap: Ldap, dataProvider: DataProvider): HttpHandler {

  val hmacFilter = HmacFilter(Hmac(Config.hmacTimeout, Config.hmacKey)).filter

  val routes = routes(
    "/" bind GET to {
      Response(OK).body("https://github.com/blindern/users-api")
    },
    "/health" bind GET to HealthController().handler,
    hmacFilter
      .then(WrappedResponse.filter)
      .then(routes(
        "/invalidate-cache" bind POST to InvalidateCache(dataProvider).handler,
        "/users" bind GET to ListUsers(dataProvider).handler,
        "/users" bind POST to { Response(NOT_IMPLEMENTED) },
        "/user/{username}" bind GET to GetUser(dataProvider).handler,
        "/user/{username}" bind POST to { Response(NOT_IMPLEMENTED) },
        "/user/{username}" bind DELETE to { Response(NOT_IMPLEMENTED) },
        "/user/{username}/groups" bind POST to { Response(NOT_IMPLEMENTED) },
        "/user/{username}/groups" bind DELETE to { Response(NOT_IMPLEMENTED) },
        "/groups" bind GET to ListGroups(dataProvider).handler,
        "/groups" bind DELETE to { Response(NOT_IMPLEMENTED) },
        "/group/{groupname}" bind GET to GetGroup(dataProvider).handler,
        "/group/{groupname}" bind POST to { Response(NOT_IMPLEMENTED) },
        "/group/{groupname}" bind DELETE to { Response(NOT_IMPLEMENTED) },
        "/auth" bind POST to { Response(NOT_IMPLEMENTED) },
        "/simpleauth" bind POST to SimpleAuth(ldap, dataProvider).handler
      ))
  )

  return ServerFilters.CatchLensFailure()
    .then(routes)
}
