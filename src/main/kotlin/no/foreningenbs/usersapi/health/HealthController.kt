package no.foreningenbs.usersapi.health

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Moshi.auto
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Instant
import no.foreningenbs.BuildConfig

class HealthController {
  private val bodyLens = Body.auto<HealthResponse>().toLens()
  private val response = HealthResponse()

  val handler: HttpHandler = {
    Response(OK)
      .with(
        bodyLens of response
      )
  }
}

data class BuildInfo(
  val timestamp: String = BuildConfig.BUILD_TIME,
  val gitCommit: String = BuildConfig.GIT_COMMIT
)

data class HealthResponse(
  val build: BuildInfo = BuildInfo(),
  val host: String =
    try {
      InetAddress.getLocalHost().hostName
    } catch (e: UnknownHostException) {
      "unknown"
    },
  val startTime: String =
    Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().startTime).toString()
)
