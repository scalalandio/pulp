package io.scalaland.pulp

import org.scalatest.FlatSpec

class ImplementedAsSpec extends FlatSpec {

  behavior of "@ImplementedAs annotation"

  @ImplementedAs(classOf[MonoNoCompanionImpl]) trait MonoNoCompanion
  class MonoNoCompanionImpl extends MonoNoCompanion

  it should "generate implicit Provider def for a monomorphic class without a companion" in {
    // given
    implicit val _: Provider[MonoNoCompanionImpl] = Provider.value(new MonoNoCompanionImpl)

    // when

    // then
    assertCompiles("Provider.get[MonoNoCompanion]")
  }

  @ImplementedAs(classOf[MonoCompanionImpl]) trait MonoCompanion
  object MonoCompanion
  class MonoCompanionImpl extends MonoCompanion

  object MonoCompanion2
  @ImplementedAs(classOf[MonoCompanion2Impl]) trait MonoCompanion2
  class MonoCompanion2Impl extends MonoCompanion2

  it should "generate implicit Provider def for a monomorphic class with a companion" in {
    // given
    implicit val _: Provider[MonoCompanionImpl] = Provider.value(new MonoCompanionImpl)
    implicit val _2: Provider[MonoCompanion2Impl] = Provider.value(new MonoCompanion2Impl)

    // when

    // then
    assertCompiles("Provider.get[MonoCompanion]")
    assertCompiles("Provider.get[MonoCompanion2]")
  }
}
