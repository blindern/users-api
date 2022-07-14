package no.foreningenbs.usersapi.api

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import no.foreningenbs.usersapi.Config
import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.createLdapMock
import org.http4k.core.Method
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object InvalidateCacheSpec : Spek({
  val ldap = createLdapMock()
  val dataProvider = DataProvider(Config, ldap)
  val handler = InvalidateCache(dataProvider).handler

  describe("cache invalidation") {
    it("should invalidate cache") {
      val data = dataProvider.getData()

      every { ldap.getGroups(any()) } returns mapOf()
      every { ldap.getUsers(any()) } returns mapOf()

      dataProvider.getData() shouldBe data

      handler(org.http4k.core.Request(Method.POST, "/dummy"))

      dataProvider.getData() shouldNotBe data
    }
  }
})
