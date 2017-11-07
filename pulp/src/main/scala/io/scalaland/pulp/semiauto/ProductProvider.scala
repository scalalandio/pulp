package io.scalaland.pulp.semiauto

import io.scalaland.pulp.Provider
import shapeless.{::, HList}

final class ProductProvider[A <: HList](thunk: => A) {

  def get: A = thunk

  def ::[B](provider: Provider[B]): ProductProvider[B :: A] = new ProductProvider(provider.get :: get)
}
