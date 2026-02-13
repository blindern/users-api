package no.foreningenbs.usersapi.hmac

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import no.foreningenbs.usersapi.AuthFilter.Companion.hmacHashHeader
import no.foreningenbs.usersapi.AuthFilter.Companion.hmacTimeHeader
import no.foreningenbs.usersapi.AuthFilter.Companion.hmacVersionHeader
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.with
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Hmac(
  private val timeout: Long,
  private val key: String,
) {
  private val moshi =
    Moshi
      .Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

  fun isValidTime(time: Long): Boolean {
    val now = Instant.now().epochSecond
    return ((now - timeout)..(now + timeout)).contains(time)
  }

  fun prepareFormPayload(vars: Map<String, List<String?>>): Any {
    val flatVars =
      vars
        .mapNotNull { (key, list) ->
          // Only pick first variable if multiple is given
          list.firstOrNull()?.let { key to it }
        }.toMap()

    // In the old PHP version, an empty map results in an empty array.
    return if (flatVars.isEmpty()) {
      emptyList<String>()
    } else {
      flatVars
    }
  }

  fun withHmac(req: Request): Request {
    val time = Instant.now().epochSecond
    val body = req.bodyString()

    return req
      .with(
        hmacHashHeader of generateHashV2(time, req.method, req.uri, body),
      ).with(hmacTimeHeader of time)
      .with(hmacVersionHeader of "2")
  }

  /**
   * V1: Legacy JSON-array-based HMAC (for backward compatibility with form-based callers).
   */
  fun generateHash(
    time: Long,
    method: Method,
    uri: Uri,
    payload: Any,
  ): String {
    val dataList =
      listOf(
        time.toString(),
        method.toString(),
        uri.toString(),
        payload,
      )

    // Escape forward slash as PHPs json_encode also does it,
    // as we need to maintain compatibility with old HMAC code.
    val data = moshi.adapter(List::class.java).toJson(dataList).replace("/", "\\/")

    return hmacSha256(data)
  }

  /**
   * V2: Conventional newline-separated canonical string HMAC.
   * Signs: METHOD\nPATH\nTIMESTAMP\nBODY
   */
  fun generateHashV2(
    time: Long,
    method: Method,
    uri: Uri,
    body: String,
  ): String {
    val data = "$method\n$uri\n$time\n$body"
    return hmacSha256(data)
  }

  private fun hmacSha256(data: String): String {
    val hasher = Mac.getInstance("HmacSHA256")
    hasher.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
    return hex(hasher.doFinal(data.toByteArray())!!)
  }

  private fun hex(data: ByteArray) =
    data
      .fold(StringBuilder()) { acc, next ->
        acc.append(String.format("%02x", next))
      }.toString()
      .lowercase()

  companion object {
    const val HASH_HEADER = "X-API-Hash"
    const val TIME_HEADER = "X-API-Time"
    const val VERSION_HEADER = "X-API-Hash-Version"
  }
}
