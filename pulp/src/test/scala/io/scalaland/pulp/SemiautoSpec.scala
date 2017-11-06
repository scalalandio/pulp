package io.scalaland.pulp

import org.scalatest.FlatSpec

class SemiautoSpec extends FlatSpec {

  behavior of "import semiauto._ should allow Provider derivation"

  it should "derive Provider for class using its constructor arguments" in {
    // given
    @Wired
    class A
    @Wired
    class B
    class C (val a: A, val b: B)

    // when
    import semiauto._
    import shapeless._

    // then
    assertCompiles("Generic[C]")
    assertCompiles("Provider.get[HNil]")
    assertCompiles("Provider.get[B :: HNil]")
    assertCompiles("Provider.get[A :: B :: HNil]")
    assertCompiles("Provider.get[C]")
  }
}
