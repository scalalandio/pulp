package io.scalaland.pulp

import org.scalatest.FlatSpec

class FactorySpec extends FlatSpec {

  behavior of "@Factory annotation"

  it should "generate implicit Provider def for a monomorphic class without a companion" in {
    // given
    // when
    @Factory class MonoNoCompanion

    // then
    assertCompiles("Provider.get[MonoNoCompanion]")
  }

  it should "generate implicit Provider def for a monomorphic class with a companion" in {
    // given
    // when
    @Factory class MonoCompanion
    object MonoCompanion

    object MonoCompanion2
    @Factory class MonoCompanion2

    // then
    assertCompiles("Provider.get[MonoCompanion]")
    assertCompiles("Provider.get[MonoCompanion2]")
  }

  it should "generate implicit Provider def for a polymorphic class without a companion" in {
    // given
    // when
    @Factory class PolyNoCompanion[T]

    // then
    assertCompiles("Provider.get[PolyNoCompanion[String]]")
  }

  it should "generate implicit Provider def for a polymorphic class with a companion" in {
    // given
    // when
    @Factory class PolyCompanion[T]
    object PolyCompanion

    object PolyCompanion2
    @Factory class PolyCompanion2[T]

    // then
    assertCompiles("Provider.get[PolyCompanion[String]]")
    assertCompiles("Provider.get[PolyCompanion2[String]]")
  }

  it should "generate implicit Provider def for a class with parameters" in {
    // given
    implicit val stringProvider: Provider[String] = Provider.value("test")
    implicit val intProvider:    Provider[Int] = Provider.value(10)
    implicit val doubleProvider: Provider[Double] = Provider.value(20.4)

    // when
    @Factory class ComplexCase[T](t: T, name: String, size: Int)

    // then
    assertCompiles("Provider.get[ComplexCase[Double]]")
  }
}
