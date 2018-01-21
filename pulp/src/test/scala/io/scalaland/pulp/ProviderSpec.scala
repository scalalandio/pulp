package io.scalaland.pulp

import org.scalatest.FlatSpec

class ProviderSpec extends FlatSpec {

  trait Test
  "Provider.liftImplicit" should "lift implicit A value to implicit Provider[A]" in {
    // given
    implicit val test = new Test {}

    // when

    // then
    assertCompiles("Provider.get[Test]")
  }
}
