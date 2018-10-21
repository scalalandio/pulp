package io.scalaland.pulp

import io.scalaland.pulp.internals._

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Cached extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Cached.impl
}

private object Cached {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = WiredImpl.impl(WiredImpl.Type.Cached)(c)(annottees)
}
