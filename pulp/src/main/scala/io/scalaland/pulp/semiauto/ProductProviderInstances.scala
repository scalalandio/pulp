package io.scalaland.pulp.semiauto

import io.scalaland.pulp.Provider
import shapeless.{::, Generic, HList, HNil}

trait ProductProviderInstances {

  implicit def product[A, AG <: HList](implicit aRepr: Generic.Aux[A, AG], agP: ProductProvider[AG]): Provider[A] =
    Provider.factory(aRepr.from(agP.get))

  implicit val hnil: ProductProvider[HNil] = new ProductProvider(HNil)

  implicit def hcons[H, T <: HList](implicit headP: Provider[H], tailP: ProductProvider[T]): ProductProvider[H :: T] =
    headP :: tailP
}
