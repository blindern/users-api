package no.foreningenbs.usersapi.health

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Moshi.auto
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Instant
import java.util.Properties

class HealthController {
  private val bodyLens = Body.auto<HealthResponse>().toLens()
  private val response = HealthResponse()

  val handler: HttpHandler = {
    Response(OK)
      .with(
        bodyLens of response,
      )
  }
}

data class HealthResponse(
  val build: HealthBuildInfo = getHealthBuildInfo(),
  val host: String =
    try {
      InetAddress.getLocalHost().hostName
    } catch (e: UnknownHostException) {
      "unknown"
    },
  val startTime: String =
    Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().startTime).toString(),
)

data class HealthBuildInfo(
  /**
   * During local development this will be null.
   */
  val timestamp: Instant?,
  val commit: String?,
  val url: String?,
)

/**
 * Create [HealthBuildInfo] based on build.properties injected by the build.
 */
fun getHealthBuildInfo(): HealthBuildInfo {
  val f = File("/build.properties")
  if (!f.exists()) {
    return HealthBuildInfo(
      timestamp = null,
      commit = null,
      url = null,
    )
  }

  val properties =
    Properties().apply {
      f.bufferedReader().use {
        load(it)
      }
    }

  return HealthBuildInfo(
    timestamp =
      properties.getProperty("build.timestamp").let {
        if (it.isEmpty()) null else Instant.parse(it)
      },
    commit = properties.getProperty("build.commit"),
    url = properties.getProperty("build.url"),
  )
}
