package no.foreningenbs.usersapi.api

import no.foreningenbs.usersapi.DataProvider
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

class InvalidateCache(private val dataProvider: DataProvider) {
  val handler: HttpHandler = {
    dataProvider.invalidateCache()
    Response(Status.OK)
  }
}
