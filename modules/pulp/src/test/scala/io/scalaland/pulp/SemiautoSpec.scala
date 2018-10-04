package io.scalaland.pulp

import org.specs2.mutable.Specification

class SemiautoSpec extends Specification {

  "import semiauto._" should {

    "allow deriving Provider for class using its constructor arguments" in {
      // given
      @Wired
      class A
      @Wired
      class B
      class C(val a: A, val b: B)

      // when
      import semiauto._

      // then
      Provider.get[C] must not(beNull)
    }
  }
}
