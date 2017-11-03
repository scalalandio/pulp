package io.scalaland.pulp

import scala.annotation.{implicitAmbiguous, implicitNotFound}

@implicitAmbiguous("Provider[$T] is ambiguous - check your scope for redundant Provider[$T] val/def")
@implicitNotFound("Provider[$T] not found, add annotation to $T and/or provide implicit Providers for constructor args")
trait Provider[+T] {

  def get: T
}

object Provider {

  def get[T: Provider]: T = implicitly[Provider[T]].get

  def factory[T](value: => T): Provider[T] = new Provider[T] { def get: T = value }
  def value[T](value: => T): Provider[T] = new Provider[T] { lazy val get: T = value }

  def upcast[T : Provider, U >: T]: Provider[U] = implicitly[Provider[T]]
}
