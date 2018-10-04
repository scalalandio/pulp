package io.scalaland.pulp.semiauto

import io.scalaland.pulp.Provider

trait ProviderInstances {

  import shapeless._

  implicit def genericProvider[A, R <: HList](implicit generic: Generic.Aux[A, R], rP: Lazy[Provider[R]]): Provider[A] =
    for { r <- rP.value } yield generic.from(r)

  implicit def hconsProvider[H, T <: HList](implicit headP: Lazy[Provider[H]], tailP: Provider[T]): Provider[H :: T] =
    for { head <- headP.value; tail <- tailP } yield head :: tail

  implicit val hnilProvider: Provider[HNil] = Provider.const(HNil)
}
