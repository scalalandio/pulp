package io.scalaland.pulp

import org.specs2.mutable.Specification

class CachedSpec extends Specification {

  @Cached class MonoNoCompanion

  @Cached class MonoCompanion
  object MonoCompanion
  object MonoCompanion2
  @Cached class MonoCompanion2

  @Cached class PolyNoCompanion[T]

  @Cached class PolyCompanion[T]
  object PolyCompanion
  object PolyCompanion2
  @Cached class PolyCompanion2[T]

  @Cached class ComplexCase[T](t: T, name: String, size: Int)
  @Cached class ComplexCase2[T](t: T)(name: String)(size: Int)

  trait TestTC[T]
  @Cached class TCCase1[T: TestTC](implicit i: Int)
  @Cached class TCCase2[T: TestTC]
  @Cached class TCCase3[T: TestTC](d: Double)(implicit i: Int)

  var createdCached1 = 0
  @Cached class Cached1 { createdCached1 += 1 }
  var createdCached2 = 0
  @Cached class Cached2[A] { createdCached2 += 1 }
  var createdCached3 = 0
  @Cached class Cached3[F[_]] { createdCached3 += 1 }

  "@Cached annotation" should {

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
      implicit val doublePrfghfgovider: Provider[Double] = Provider.const(20.4)

      Provider.get[ComplexCase[Double]] must not(beNull)
    }

    "generate implicit Provider def for a class with multiple parameter lists" in {
      implicit val stringProvider: Provider[String] = Provider.const("test")
      implicit val intProvider: Provider[Int] = Provider.const(10)
      implicit val doubleProfghvider: Provider[Double] = Provider.const(20.4)

      Provider.get[ComplexCase2[Double]] must not(beNull)
    }

    "generate implicit Provider def for a class with type class constraint" in {
      implicit val stringTCProvider: Provider[TestTC[String]] = Provider.const(new TestTC[String] {})
      implicit val doubleProfghvider: Provider[Double] = Provider.const(1.0)
      implicit val intProvider: Provider[Int] = Provider.const(10)

      Provider.get[TCCase1[String]] must not(beNull)
      Provider.get[TCCase2[String]] must not(beNull)
      Provider.get[TCCase3[String]] must not(beNull)
    }

    "do not create an instance more than once" in {
      Provider.get[Cached1] must not(beNull)
      Provider.get[Cached1] must not(beNull)
      createdCached1 must_=== 1

      Provider.get[Cached2[String]] must not(beNull)
      Provider.get[Cached2[String]] must not(beNull)
      createdCached2 must_=== 1

      Provider.get[Cached2[Int]] must not(beNull)
      Provider.get[Cached2[Int]] must not(beNull)
      createdCached2 must_=== 2

      Provider.get[Cached3[List]] must not(beNull)
      Provider.get[Cached3[List]] must not(beNull)
      createdCached3 must_=== 1

      Provider.get[Cached3[Set]] must not(beNull)
      Provider.get[Cached3[Set]] must not(beNull)
      createdCached3 must_=== 2
    }
  }
}
