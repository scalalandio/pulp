package io.scalaland.pulp

trait PlatformSpecific {

  def cached[A](value: => A)(implicit tag: internals.Cache.Id[A]): Provider[A] =
    new Provider[A] { def get: A = internals.Cache.query(tag, value) }
}
