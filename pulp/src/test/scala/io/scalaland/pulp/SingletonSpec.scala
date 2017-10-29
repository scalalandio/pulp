package io.scalaland.pulp

import org.scalatest.FlatSpec

class SingletonSpec extends FlatSpec {

  behavior of "@Singleton annotation"

  it should "generate implicit Provider def for a monomorphic class without a companion" in {
    // given
    // when
    @Singleton class MonoNoCompanion

    // then
    assertCompiles("Provider.get[MonoNoCompanion]")
  }

  it should "generate implicit Provider def for a monomorphic class with a companion" in {
    // given
    // when
    @Singleton class MonoCompanion
    object MonoCompanion

    object MonoCompanion2
    @Singleton class MonoCompanion2

    // then
    assertCompiles("Provider.get[MonoCompanion]")
    assertCompiles("Provider.get[MonoCompanion2]")
  }
}

