package io.scalaland.pulp

import scala.annotation.implicitNotFound

@implicitNotFound("Provider[${A}] not found, add annotation to ${A} or provide implicit Providers for constructor args")
trait Provider[A] {

  def get: A

  def map[B](f: A => B): Provider[B] = flatMap(t => Provider.factory(f(t)))
  def flatMap[B](f: A => Provider[B]): Provider[B] = f(get)
}

object Provider extends PlatformSpecific {

  @inline def apply[A: Provider]: Provider[A] = implicitly[Provider[A]]
  @inline def get[A: Provider]: A = apply[A].get

  def const[A](value: => A): Provider[A] = new Provider[A] { lazy val get: A = value }
  def factory[A](value: => A): Provider[A] = new Provider[A] { def get: A = value }

  @inline def upcast[A: Provider, B >: A]: Provider[B] = apply[A].map(identity)

  implicit def liftImplicit[A](implicit value: A): Provider[A] = const(value)
}
