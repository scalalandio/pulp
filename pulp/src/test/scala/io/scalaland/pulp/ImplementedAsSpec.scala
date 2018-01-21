package io.scalaland.pulp

import org.scalatest.FlatSpec

class ImplementedAsSpec extends FlatSpec {

  behavior of "@ImplementedAs annotation"

  @ImplementedAs[MonoNoCompanionImpl]
  trait MonoNoCompanion
  class MonoNoCompanionImpl extends MonoNoCompanion

  it should "generate implicit Provider def for a monomorphic class without a companion" in {
    // given
    implicit val _: Provider[MonoNoCompanionImpl] = Provider.const(new MonoNoCompanionImpl)

    // when

    // then
    assertCompiles("Provider.get[MonoNoCompanion]")
  }

  @ImplementedAs[MonoCompanionImpl]
  trait MonoCompanion
  object MonoCompanion
  class MonoCompanionImpl extends MonoCompanion

  object MonoCompanion2
  @ImplementedAs[MonoCompanion2Impl]
  trait MonoCompanion2
  class MonoCompanion2Impl extends MonoCompanion2

  it should "generate implicit Provider def for a monomorphic class with a companion" in {
    // given
    implicit val _: Provider[MonoCompanionImpl] = Provider.const(new MonoCompanionImpl)
    implicit val _2: Provider[MonoCompanion2Impl] = Provider.const(new MonoCompanion2Impl)

    // when

    // then
    assertCompiles("Provider.get[MonoCompanion]")
    assertCompiles("Provider.get[MonoCompanion2]")
  }

  @ImplementedAs[PolyNoCompanionImpl[T]] trait PolyNoCompanion[T]
  @Wired class PolyNoCompanionImpl[T] extends PolyNoCompanion[T]

  it should "generate implicit Provider def for a polymorphic class without a companion" in {
    // given
    implicit val _: Provider[String] = Provider.const("")

    // when

    // then
    assertCompiles("Provider.get[PolyNoCompanion[String]]")
  }

  @ImplementedAs[PolyCompanionImpl[T]] trait PolyCompanion[T]
  object PolyCompanion
  @Wired class PolyCompanionImpl[T] extends PolyCompanion[T]

  it should "generate implicit Provider def for a polymorphic class a companion" in {
    // given
    implicit val _: Provider[String] = Provider.const("")

    // when

    // then
    assertCompiles("Provider.get[PolyCompanion[String]]")
  }
}
