package io.scalaland.pulp

import scala.annotation.{implicitAmbiguous, implicitNotFound}

@implicitAmbiguous("Provider[${T}] is ambiguous - check your scope for redundant Provider[$T] val/def")
@implicitNotFound("Provider[${T}] not found, add annotation to ${T} or provide implicit Providers for constructor args")
trait Provider[+T] {

  def get: T

  def map[U](f: T => U): Provider[U] = flatMap(t => Provider.factory(f(t)))
  def flatMap[U](f: T => Provider[U]): Provider[U] = f(get)
}

object Provider {

  @inline def apply[T: Provider]: Provider[T] = implicitly[Provider[T]]
  @inline def get[T: Provider]: T = apply[T].get

  def factory[T](value: => T): Provider[T] = new Provider[T] { def get: T = value }
  def value[T](value: => T): Provider[T] = new Provider[T] { lazy val get: T = value }

  @inline def upcast[T: Provider, U >: T]: Provider[U] = apply[T]
}
