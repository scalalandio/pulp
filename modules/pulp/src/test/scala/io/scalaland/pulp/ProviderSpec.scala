package io.scalaland.pulp

import org.specs2.mutable.Specification

class ProviderSpec extends Specification {

  trait Test
  "Provider.liftImplicit" should {

    "lift implicit A value to implicit Provider[A]" in {
      // given
      implicit val test = new Test {}

      // when

      // then
      Provider.get[Test] must not(beNull)
    }
  }
}
