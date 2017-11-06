package io.scalaland.pulp

import shapeless.{::, Generic, HList, HNil}

package object semiauto {

  implicit def derive[A, GenA <: HList](implicit generic: Generic.Aux[A, GenA], genAP: Provider[GenA]): Provider[A] =
    for { genA <- genAP } yield generic.from(genA)

  implicit val hnil: Provider[HNil] = Provider.factory(HNil)

  implicit def hcons[H, T <: HList](implicit headP: Provider[H], tailP: Provider[T]): Provider[H :: T] =
    for { head <- headP; tail <- tailP } yield head :: tail
}
