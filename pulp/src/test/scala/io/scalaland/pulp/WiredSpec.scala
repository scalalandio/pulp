package io.scalaland.pulp

import org.scalatest.FlatSpec

class WiredSpec extends FlatSpec {

  behavior of "@Wired annotation"

  it should "generate implicit Provider def for a monomorphic class without a companion" in {
    // given
    // when
    @Wired class MonoNoCompanion

    // then
    assertCompiles("Provider.get[MonoNoCompanion]")
  }

  it should "generate implicit Provider def for a monomorphic class with a companion" in {
    // given
    // when
    @Wired class MonoCompanion
    object MonoCompanion

    object MonoCompanion2
    @Wired class MonoCompanion2

    // then
    assertCompiles("Provider.get[MonoCompanion]")
    assertCompiles("Provider.get[MonoCompanion2]")
  }

  it should "generate implicit Provider def for a polymorphic class without a companion" in {
    // given
    // when
    @Wired class PolyNoCompanion[T]

    // then
    assertCompiles("Provider.get[PolyNoCompanion[String]]")
  }

  it should "generate implicit Provider def for a polymorphic class with a companion" in {
    // given
    // when
    @Wired class PolyCompanion[T]
    object PolyCompanion

    object PolyCompanion2
    @Wired class PolyCompanion2[T]

    // then
    assertCompiles("Provider.get[PolyCompanion[String]]")
    assertCompiles("Provider.get[PolyCompanion2[String]]")
  }

  it should "generate implicit Provider def for a class with parameters" in {
    // given
    implicit val stringProvider: Provider[String] = Provider.const("test")
    implicit val intProvider: Provider[Int] = Provider.const(10)
    implicit val doubleProvider: Provider[Double] = Provider.const(20.4)

    // when
    @Wired class ComplexCase[T](t: T, name: String, size: Int)

    // then
    assertCompiles("Provider.get[ComplexCase[Double]]")
  }

  it should "generate implicit Provider def for a class with multiple parameter lists" in {
    // given
    implicit val stringProvider: Provider[String] = Provider.const("test")
    implicit val intProvider: Provider[Int] = Provider.const(10)
    implicit val doubleProvider: Provider[Double] = Provider.const(20.4)

    // when
    @Wired class ComplexCase[T](t: T)(name: String)(size: Int)

    // then
    assertCompiles("Provider.get[ComplexCase[Double]]")
  }
}
