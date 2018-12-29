package no.foreningenbs.usersapi.hmac

import no.foreningenbs.usersapi.hmac.Hmac.Companion.HASH_HEADER
import no.foreningenbs.usersapi.hmac.Hmac.Companion.TIME_HEADER
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.formAsMap
import org.http4k.lens.Header
import org.http4k.lens.long

class HmacFilter(private val hmac: Hmac) {
  val filter = Filter { next ->
    filter@{ req ->
      // Verify given time is within threshold (limits replay window)
      if (!hmac.isValidTime(timeHeader(req))) {
        return@filter Response(Status.UNAUTHORIZED)
          .body("HMAC-authorization failed: Time check failed")
      }

      // Due to running behind a reverse proxy serving us on /users-api
      // publicly, we validate both internal and public HMAC urls.
      if (genHash(req) != hashHeader(req) && genHash(req, "/users-api") != hashHeader(req)) {
        return@filter Response(Status.UNAUTHORIZED)
          .body("HMAC-authorization failed: Invalid hash")
      }

      next(req)
    }
  }

  fun genHash(req: Request, pathPrefix: String = "") =
    hmac.generateHash(
      timeHeader(req),
      req.method,
      req.uri.path(pathPrefix + req.uri.path),
      req.formAsMap()
    )

  companion object {
    val hashHeader = Header.required(HASH_HEADER)
    val timeHeader = Header.long().required(TIME_HEADER)
  }
}
