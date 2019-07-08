package io.scalaland.pulp

import org.specs2.mutable.Specification

class ImplementedAsSpec extends Specification {

  "@ImplementedAs annotation" should {

    @ImplementedAs[MonoNoCompanionImpl]
    trait MonoNoCompanion
    class MonoNoCompanionImpl extends MonoNoCompanion

    "generate implicit Provider def for a monomorphic class without a companion" in {
      // given
      implicit val _1: Provider[MonoNoCompanionImpl] = Provider.const(new MonoNoCompanionImpl)

      // when

      // then
      Provider.get[MonoNoCompanion] must not(beNull)
    }

    @ImplementedAs[MonoCompanionImpl]
    trait MonoCompanion
    object MonoCompanion
    class MonoCompanionImpl extends MonoCompanion

    object MonoCompanion2
    @ImplementedAs[MonoCompanion2Impl]
    trait MonoCompanion2
    class MonoCompanion2Impl extends MonoCompanion2

    "generate implicit Provider def for a monomorphic class with a companion" in {
      // given
      implicit val _1: Provider[MonoCompanionImpl] = Provider.const(new MonoCompanionImpl)
      implicit val _2: Provider[MonoCompanion2Impl] = Provider.const(new MonoCompanion2Impl)

      // when

      // then
      Provider.get[MonoCompanion] must not(beNull)
      Provider.get[MonoCompanion2] must not(beNull)
    }

    @ImplementedAs[PolyNoCompanionImpl[T]]
    trait PolyNoCompanion[T]
    @Wired class PolyNoCompanionImpl[T] extends PolyNoCompanion[T]

    "generate implicit Provider def for a polymorphic class without a companion" in {
      // given
      implicit val _1: Provider[String] = Provider.const("")

      // when

      // then
      Provider.get[PolyNoCompanion[String]] must not(beNull)
    }

    @ImplementedAs[PolyCompanionImpl[T]]
    trait PolyCompanion[T]
    object PolyCompanion
    @Wired class PolyCompanionImpl[T] extends PolyCompanion[T]

    "generate implicit Provider def for a polymorphic class a companion" in {
      // given
      implicit val _1: Provider[String] = Provider.const("")

      // when

      // then
      Provider.get[PolyCompanion[String]] must not(beNull)
    }
  }
}
