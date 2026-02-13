package no.foreningenbs.usersapi.api

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import no.foreningenbs.usersapi.Config
import no.foreningenbs.usersapi.DataProvider
import no.foreningenbs.usersapi.createLdapMock
import org.http4k.core.Method
import org.http4k.core.Request

class InvalidateCacheSpec :
  DescribeSpec({
    val ldap = createLdapMock()
    val dataProvider = DataProvider(Config, ldap)
    val handler = InvalidateCache(dataProvider).handler

    describe("cache invalidation") {
      it("should invalidate cache") {
        val data = dataProvider.getData()

        every { ldap.getGroups(any()) } returns mapOf()
        every { ldap.getUsers(any()) } returns mapOf()

        dataProvider.getData() shouldBe data

        handler(Request(Method.POST, "/dummy"))

        dataProvider.getData() shouldNotBe data
      }
    }
  })
