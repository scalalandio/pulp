package io.scalaland.pulp.semiauto

import shapeless.HList

final class ProductProvider[A <: HList](val get: A) extends AnyVal
