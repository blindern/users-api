package no.foreningenbs.usersapi

import com.github.benmanes.caffeine.cache.Caffeine
import no.foreningenbs.usersapi.ldap.AllData
import no.foreningenbs.usersapi.ldap.Ldap

class DataProvider(
  config: Config,
  private val ldap: Ldap,
) {
  private val cache =
    Caffeine
      .newBuilder()
      .expireAfterWrite(config.cacheTimeout)
      .build<String, AllData> {
        ldap.getAllData()
      }

  fun getData(): AllData = cache["cache"]

  fun invalidateCache() {
    cache.invalidateAll()
  }
}
