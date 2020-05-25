package no.foreningenbs.usersapi.hmac

import org.amshove.kluent.shouldBeEqualTo
import org.http4k.core.Method
import org.http4k.core.Uri
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object HmacSpec : Spek({
  describe("generateHash") {
    it("should generate expected hash") {
      val hmac = Hmac(100, "some-key")
      val generated = hmac.generateHash(
        1546066592L,
        Method.GET,
        Uri.of("/users"),
        mapOf()
      )

      val expected = "8623a8a04b33f9e02e0eb0654ea658420b2da418b5755a72d4cc231649c1aa7a"

      generated shouldBeEqualTo expected
    }
  }
})
