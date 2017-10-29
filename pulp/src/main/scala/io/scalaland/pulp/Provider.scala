package io.scalaland.pulp

trait Provider[T] {

  def get(): T
}

object Provider {

  def get[T: Provider]: T = implicitly[Provider[T]].get()

  def instance[T](value: => T): Provider[T] = () => value
}
