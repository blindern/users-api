package no.foreningenbs.usersapi

import io.github.oshai.kotlinlogging.KotlinLogging
import no.foreningenbs.usersapi.hmac.Hmac
import no.foreningenbs.usersapi.hmac.Hmac.Companion.HASH_HEADER
import no.foreningenbs.usersapi.hmac.Hmac.Companion.TIME_HEADER
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.formAsMap
import org.http4k.lens.Header
import org.http4k.lens.long

private val logger = KotlinLogging.logger {}

class AuthFilter(
  private val hmac: Hmac,
  private val authKey: String,
  private val enforceAuth: Boolean,
) {
  val filter =
    Filter { next ->
      filter@{ req ->
        // If a method is used, it must be valid.

        // HMAC
        hmacHashHeader(req)?.let { hmacHashHeader ->
          // Verify given time is within threshold (limits replay window)
          if (!hmac.isValidTime(hmacTimeHeader(req))) {
            return@filter Response(Status.UNAUTHORIZED)
              .body("HMAC-authorization failed: Time check failed")
          }

          // Due to running behind a reverse proxy serving us on /users-api
          // publicly, we validate both internal and public HMAC urls.
          if (
            generateHmacHash(req) != hmacHashHeader &&
            generateHmacHash(req, "/users-api") != hmacHashHeader
          ) {
            return@filter Response(Status.UNAUTHORIZED)
              .body("HMAC-authorization failed: Invalid hash")
          }

          return@filter next(req)
        }

        // Bearer
        getBearer(req)?.let { bearerToken ->
          return@filter if (bearerToken == authKey) {
            next(req)
          } else {
            Response(Status.UNAUTHORIZED).body("Invalid credentials")
          }
        }

        // Special case for development.
        if (!enforceAuth) {
          logger.warn { "Request served without authentication due to configuration override" }
          next(req)
        } else {
          Response(Status.UNAUTHORIZED).body("No authentication given")
        }
      }
    }

  private fun generateHmacHash(
    req: Request,
    pathPrefix: String = "",
  ) = hmac.generateHash(
    hmacTimeHeader(req),
    req.method,
    req.uri.path(pathPrefix + req.uri.path),
    req.formAsMap(),
  )

  private fun getBearer(req: Request) =
    bearerHeader(req)?.let {
      val prefix = "bearer "
      if (it.startsWith(prefix, ignoreCase = true)) {
        it.substring(prefix.length)
      } else {
        null
      }
    }

  companion object {
    val hmacHashHeader = Header.optional(HASH_HEADER)
    val hmacTimeHeader = Header.long().required(TIME_HEADER)
    val bearerHeader = Header.optional("Authorization")
  }
}
