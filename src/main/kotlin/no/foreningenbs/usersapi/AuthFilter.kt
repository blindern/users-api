package no.foreningenbs.usersapi

import io.github.oshai.kotlinlogging.KotlinLogging
import no.foreningenbs.usersapi.hmac.Hmac
import no.foreningenbs.usersapi.hmac.Hmac.Companion.HASH_HEADER
import no.foreningenbs.usersapi.hmac.Hmac.Companion.TIME_HEADER
import no.foreningenbs.usersapi.hmac.Hmac.Companion.VERSION_HEADER
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.formAsMap
import org.http4k.lens.Header
import org.http4k.lens.long
import java.security.MessageDigest

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
            !constantTimeEquals(generateHmacHash(req), hmacHashHeader) &&
            !constantTimeEquals(generateHmacHash(req, "/users-api"), hmacHashHeader)
          ) {
            return@filter Response(Status.UNAUTHORIZED)
              .body("HMAC-authorization failed: Invalid hash")
          }

          return@filter next(req)
        }

        // Bearer
        getBearer(req)?.let { bearerToken ->
          return@filter if (constantTimeEquals(bearerToken, authKey)) {
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
  ): String {
    val version = hmacVersionHeader(req)
    val time = hmacTimeHeader(req)
    val uri = req.uri.path(pathPrefix + req.uri.path)

    return if (version == "2") {
      hmac.generateHashV2(time, req.method, uri, req.bodyString())
    } else {
      // V1 legacy: JSON-array-based
      val payload =
        if (req.header("Content-Type")?.contains("application/json") == true) {
          req.bodyString()
        } else {
          hmac.prepareFormPayload(req.formAsMap())
        }
      hmac.generateHash(time, req.method, uri, payload)
    }
  }

  private fun constantTimeEquals(
    a: String,
    b: String,
  ): Boolean = MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

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
    val hmacVersionHeader = Header.optional(VERSION_HEADER)
    val bearerHeader = Header.optional("Authorization")
  }
}
