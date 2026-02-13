package no.foreningenbs.usersapi.hmac

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Uri

class HmacSpec :
  DescribeSpec({
    describe("generateHash") {
      it("should generate expected hash") {
        val hmac = Hmac(100, "some-key")
        val generated =
          hmac.generateHash(
            1546066592L,
            Method.GET,
            Uri.of("/users"),
            hmac.prepareFormPayload(mapOf()),
          )

        val expected = "8623a8a04b33f9e02e0eb0654ea658420b2da418b5755a72d4cc231649c1aa7a"

        generated shouldBe expected
      }
    }
  })
