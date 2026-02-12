package no.foreningenbs.usersapi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.lens.string
import java.lang.reflect.Type

object MoshiBody {
  private val SerializeNullsFactory =
    object : JsonAdapter.Factory {
      override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi,
      ): JsonAdapter<*> = moshi.nextAdapter<Any>(this, type, annotations).serializeNulls()
    }

  private val moshi: Moshi =
    Moshi
      .Builder()
      .add(SerializeNullsFactory)
      .add(KotlinJsonAdapterFactory())
      .build()

  private inline fun <reified T : Any> asJsonBody() =
    Body
      .string(ContentType.APPLICATION_JSON)
      .map(
        {
          moshi
            .adapter(T::class.java)
            .failOnUnknown()
            .indent("  ")
            .fromJson(it)
        },
        {
          moshi
            .adapter(T::class.java)
            .failOnUnknown()
            .indent("  ")
            .toJson(it)
        },
      )

  val jsonMapLens = MoshiBody.asJsonBody<Map<String, Any?>>().toLens()
  val jsonArrayMapLens = MoshiBody.asJsonBody<Array<Map<String, Any?>>>().toLens()
}
