package no.foreningenbs.usersapi.hmac

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import no.foreningenbs.usersapi.AuthFilter.Companion.hmacHashHeader
import no.foreningenbs.usersapi.AuthFilter.Companion.hmacTimeHeader
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.body.formAsMap
import org.http4k.core.with
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Hmac(private val timeout: Long, private val key: String) {
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  fun isValidTime(time: Long): Boolean {
    val now = Instant.now().epochSecond
    return ((now - timeout)..(now + timeout)).contains(time)
  }

  fun withHmac(req: Request): Request {
    val time = Instant.now().epochSecond

    return req
      .with(
        hmacHashHeader of
          generateHash(time, req.method, req.uri, req.formAsMap())
      )
      .with(hmacTimeHeader of time)
  }

  fun generateHash(
    time: Long,
    method: Method,
    uri: Uri,
    vars: Map<String, List<String?>>
  ): String {
    val flatVars = vars
      .map { (key, list) ->
        // Only pick first variable if multiple is given
        key to list[0]
      }
      .toMap()

    val dataList = listOf(
      time.toString(),
      method.toString(),
      uri.toString(),
      // In the old PHP version, an empty map results in an empty array.
      if (flatVars.isEmpty()) emptyList<String>()
      else flatVars
    )

    // Escape forward slash as PHPs json_encode also does it,
    // as we need to maintain compatibility with old HMAC code.
    val data = moshi.adapter(List::class.java).toJson(dataList).replace("/", "\\/")

    val hasher = Mac.getInstance("HmacSHA256")
    hasher.init(SecretKeySpec(key.toByteArray(), "HmacSHA256"))
    return hex(hasher.doFinal(data.toByteArray())!!)
  }

  private fun hex(data: ByteArray) =
    data.fold(StringBuilder()) { acc, next ->
      acc.append(String.format("%02x", next))
    }.toString().toLowerCase()

  companion object {
    const val HASH_HEADER = "X-API-Hash"
    const val TIME_HEADER = "X-API-Time"
  }
}
