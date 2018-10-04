package io.scalaland.pulp

import org.specs2.mutable.Specification

class WiredSpec extends Specification {

  @Wired class MonoNoCompanion

  @Wired class MonoCompanion
  object MonoCompanion
  object MonoCompanion2
  @Wired class MonoCompanion2

  @Wired class PolyNoCompanion[T]

  @Wired class PolyCompanion[T]
  object PolyCompanion
  object PolyCompanion2
  @Wired class PolyCompanion2[T]

  @Wired class ComplexCase[T](t: T, name: String, size: Int)
  @Wired class ComplexCase2[T](t: T)(name: String)(size: Int)

  "@Wired annotation" should {

    "generate implicit Provider def for a monomorphic class without a companion" in {
      Provider.get[MonoNoCompanion] must not(beNull)
    }

    "generate implicit Provider def for a monomorphic class with a companion" in {
      Provider.get[MonoCompanion] must not(beNull)
      Provider.get[MonoCompanion2] must not(beNull)
    }

    "generate implicit Provider def for a polymorphic class without a companion" in {
      Provider.get[PolyNoCompanion[String]] must not(beNull)
    }

    "generate implicit Provider def for a polymorphic class with a companion" in {
      Provider.get[PolyCompanion[String]] must not(beNull)
      Provider.get[PolyCompanion2[String]] must not(beNull)
    }

    "generate implicit Provider def for a class with parameters" in {
      implicit val stringProvider: Provider[String] = Provider.const("test")
      implicit val intProvider: Provider[Int] = Provider.const(10)
      implicit val doubleProvider: Provider[Double] = Provider.const(20.4)

      Provider.get[ComplexCase[Double]] must not(beNull)
    }

    "generate implicit Provider def for a class with multiple parameter lists" in {
      implicit val stringProvider: Provider[String] = Provider.const("test")
      implicit val intProvider: Provider[Int] = Provider.const(10)
      implicit val doubleProvider: Provider[Double] = Provider.const(20.4)

      Provider.get[ComplexCase2[Double]] must not(beNull)
    }
  }
}
