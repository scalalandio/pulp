package io.scalaland.pulp

import org.specs2.mutable.Specification

class SingletonSpec extends Specification {

  "@Singleton annotation" should {

    "generate implicit Provider def for a monomorphic class without a companion" in {
      // given
      // when
      @Singleton class MonoNoCompanion

      // then
      Provider.get[MonoNoCompanion] must not(beNull)
    }

    "generate implicit Provider def for a monomorphic class with a companion" in {
      // given
      // when
      @Singleton class MonoCompanion
      object MonoCompanion

      object MonoCompanion2
      @Singleton class MonoCompanion2

      // then
      Provider.get[MonoCompanion] must not(beNull)
      Provider.get[MonoCompanion2] must not(beNull)
    }

    "generate implicit Provider def for a class with multiple parameter lists" in {
      // given
      implicit val stringProvider: Provider[String] = Provider.const("test")
      implicit val intProvider: Provider[Int] = Provider.const(10)

      // when
      @Singleton class ComplexCase(name: String)(size: Int)

      // then
      Provider.get[ComplexCase] must not(beNull)
    }
  }
}
