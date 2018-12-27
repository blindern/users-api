package no.foreningenbs.usersapi

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Filter
import org.http4k.lens.Header.Common.CONTENT_TYPE

object WrappedResponse {
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  val filter = Filter { next ->
    { req ->
      val res = next(req)
      val contentType = CONTENT_TYPE(res)
      if (contentType != null && contentType.value == APPLICATION_JSON.value) {
        val wrapper = Response(
          StatusBody(res.status.code, res.status.description),
          "REPLACEME"
        )

        val data = moshi.adapter(Response::class.java)
          .toJson(wrapper)
          .replace("\"REPLACEME\"", res.bodyString())

        res.body(data)
      } else {
        res
      }
    }
  }
}

data class StatusBody(val code: Int, val text: String)
data class Response(val status: StatusBody, val result: String)
