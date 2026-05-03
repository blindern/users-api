package no.foreningenbs.usersapi.ldap

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EscapeSpec :
  DescribeSpec({
    describe("escape") {
      it("escapes the five RFC 2254 special characters") {
        escape("\\") shouldBe "\\5c"
        escape("(") shouldBe "\\28"
        escape(")") shouldBe "\\29"
        escape("*") shouldBe "\\2a"
        escape("\u0000") shouldBe "\\00"
      }

      it("does not mangle digits — regression for leo04 bug where '0' was replaced with \\00") {
        escape("leo04") shouldBe "leo04"
        escape("0") shouldBe "0"
      }

      it("leaves benign input untouched") {
        escape("alice") shouldBe "alice"
        escape("user@example.com") shouldBe "user@example.com"
      }
    }
  })
